"""The local-model provider, against stand-in mlx modules: no model is loaded."""

import sys
import types
from importlib import metadata

import pytest

from mutagate.providers import local
from mutagate.providers.base import ProviderError
from mutagate.providers.local import MAX_TOKENS, MODEL, REVISION, LocalProvider

ANSWER = "```java\nclass ATest {}\n```"
REPLY = (
    "<|channel|>analysis<|message|>The mutant flips a comparison.<|end|>"
    f"<|start|>assistant<|channel|>final<|message|>{ANSWER}<|return|>"
)


class FakeTokenizer:
    def __init__(self, log):
        self.log = log

    def apply_chat_template(self, messages, **options):
        self.log["messages"], self.log["template"] = messages, options
        return "the prompt"

    def encode(self, text, add_special_tokens):
        self.log["encoded"] = (text, add_special_tokens)
        return [1, 2, 3]

    def decode(self, tokens, skip_special_tokens):
        self.log["skip_special_tokens"] = skip_special_tokens
        return "".join(tokens)


@pytest.fixture
def mlx(monkeypatch):
    """Stand-in mlx modules, and a log of how the provider called them. The model
    writes `log["reply"]` one character per token, or raises `log["error"]`."""
    log = {"reply": REPLY, "loads": [], "cleared": 0}

    def load(path, revision=None):
        log["loads"].append((path, revision))
        if "load_error" in log:
            raise log["load_error"]
        return "the model", FakeTokenizer(log)

    def stream_generate(model, tokenizer, prompt, max_tokens, sampler):
        log["generate"] = {"prompt": prompt, "max_tokens": max_tokens, "sampler": sampler}
        if "error" in log:
            raise log["error"]
        for i, piece in enumerate(log["reply"], start=1):
            yield types.SimpleNamespace(token=piece, prompt_tokens=3, generation_tokens=i)

    def clear_cache():
        log["cleared"] += 1

    mlx_lm = types.ModuleType("mlx_lm")
    mlx_lm.load, mlx_lm.stream_generate = load, stream_generate
    sample_utils = types.ModuleType("mlx_lm.sample_utils")
    sample_utils.make_sampler = lambda temp, top_p: ("sampler", temp, top_p)
    core = types.ModuleType("mlx.core")
    core.clear_cache = clear_cache
    package = types.ModuleType("mlx")
    package.core = core
    for name, module in {
        "mlx": package,
        "mlx.core": core,
        "mlx_lm": mlx_lm,
        "mlx_lm.sample_utils": sample_utils,
    }.items():
        monkeypatch.setitem(sys.modules, name, module)
    monkeypatch.setattr(local, "_downloaded", lambda model, revision: None)
    return log


def test_the_request_is_one_message_with_the_measured_settings(mlx):
    LocalProvider().complete("rules and class", "the target")
    assert mlx["messages"] == [{"role": "user", "content": "rules and class\n\nthe target"}]
    assert mlx["template"] == {
        "add_generation_prompt": True,
        "tokenize": False,
        "reasoning_effort": "medium",
    }
    assert mlx["encoded"] == ("the prompt", False)
    assert mlx["generate"] == {
        "prompt": [1, 2, 3],
        "max_tokens": MAX_TOKENS,
        "sampler": ("sampler", 1.0, 1.0),
    }
    assert mlx["skip_special_tokens"] is False
    assert mlx["cleared"] == 1


def test_the_reply_is_the_final_channel_and_its_tokens_are_kept(mlx):
    provider = LocalProvider()
    assert provider.complete("s", "t") == ANSWER
    assert provider.last_usage == {"input_tokens": 3, "output_tokens": len(REPLY)}


@pytest.mark.parametrize(
    "reply",
    [
        "<|channel|>analysis<|message|>Still reasoning when the limit",
        (
            "<|channel|>analysis<|message|>Run it.<|end|>"
            "<|start|>assistant<|channel|>commentary to=functions.bash<|message|>{}<|call|>"
        ),
    ],
    ids=["cut off at the limit", "ended on a tool call"],
)
def test_a_reply_with_no_answer_is_empty(mlx, reply):
    mlx["reply"] = reply
    assert LocalProvider().complete("s", "t") == ""


def test_a_reply_in_no_channel_stops_the_run(mlx):
    mlx["reply"] = "Text with no channel markers"
    with pytest.raises(ProviderError, match="format"):
        LocalProvider().complete("s", "t")


def test_the_model_is_loaded_once_at_the_pinned_snapshot(mlx):
    provider = LocalProvider()
    provider.complete("s", "t1")
    provider.complete("s", "t2")
    assert mlx["loads"] == [(MODEL, REVISION)]


def test_downloaded_weights_are_loaded_from_their_folder(mlx, monkeypatch):
    monkeypatch.setattr(local, "_downloaded", lambda model, revision: "/cache/snapshot")
    LocalProvider().complete("s", "t")
    assert mlx["loads"] == [("/cache/snapshot", REVISION)]


def test_a_runtime_failure_is_a_provider_error(mlx):
    mlx["error"] = RuntimeError("out of memory")
    with pytest.raises(ProviderError, match="out of memory"):
        LocalProvider().complete("s", "t")
    assert mlx["cleared"] == 1


def test_a_failed_load_is_a_provider_error(mlx):
    mlx["load_error"] = OSError("no such repository")
    with pytest.raises(ProviderError, match="could not load"):
        LocalProvider().complete("s", "t")


def test_without_mlx_lm_nothing_runs(mlx, monkeypatch):
    monkeypatch.setitem(sys.modules, "mlx_lm", None)
    with pytest.raises(ProviderError, match="pip install"):
        LocalProvider().complete("s", "t")
    assert mlx["loads"] == []


def test_settings_and_version(monkeypatch):
    provider = LocalProvider()
    assert provider.settings == {
        "model": MODEL,
        "revision": REVISION,
        "effort": "medium",
        "temperature": "1.0",
        "top_p": "1.0",
        "max_tokens": str(MAX_TOKENS),
    }
    versions = {"mlx-lm": "1", "mlx": "2", "transformers": "3"}
    monkeypatch.setattr(local.metadata, "version", lambda package: versions[package])
    assert provider.version == "mlx-lm 1, mlx 2, transformers 3"

    def missing(package):
        raise metadata.PackageNotFoundError(package)

    monkeypatch.setattr(local.metadata, "version", missing)
    with pytest.raises(ProviderError, match="pip install"):
        _ = provider.version


def test_release_frees_the_model_and_a_later_request_loads_it_again(mlx):
    provider = LocalProvider()
    provider.complete("s", "t")
    provider.release()
    assert mlx["cleared"] == 2  # after the request, then on release
    provider.complete("s", "t")
    assert len(mlx["loads"]) == 2

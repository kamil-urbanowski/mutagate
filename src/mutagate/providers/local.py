"""Local-model provider: gpt-oss-20b, run inside MutaGate through mlx-lm. Design: `docs/design/providers.md`.

mlx-lm is an optional install that runs only on Apple Silicon, so it is imported
when the first request needs it.
"""

import sys
from importlib import metadata

from .base import ProviderError

# The local model and its settings. Changing any of them after measured runs have
# started must be reported, not made quietly.
MODEL = "mlx-community/gpt-oss-20b-MXFP4-Q8"
# The weights' exact snapshot, so an update to the model's repository cannot
# change the model unnoticed.
REVISION = "773a7da77e569019bb0fd17a554b263738d669a3"
EFFORT = "medium"
TEMPERATURE = 1.0  # gpt-oss's recommended sampling
TOP_P = 1.0
MAX_TOKENS = 8192  # the reply's limit, reasoning included

INSTALL = 'install it on an Apple Silicon Mac with `pip install -e ".[local]"`'

# gpt-oss writes each part of a reply in its own channel: its reasoning, any tool
# call, and its answer ("final"). Only the answer is the reply.
_CHANNEL = "<|channel|>"
_FINAL = "<|channel|>final<|message|>"
_ENDS = ("<|return|>", "<|end|>", "<|call|>")


class LocalProvider:
    name = "local"

    def __init__(
        self,
        model: str = MODEL,
        revision: str = REVISION,
        effort: str = EFFORT,
        max_tokens: int = MAX_TOKENS,
    ):
        self.max_tokens = max_tokens
        self.settings = {
            "model": model,
            "revision": revision,
            "effort": effort,
            "temperature": str(TEMPERATURE),
            "top_p": str(TOP_P),
            "max_tokens": str(max_tokens),
        }
        self.last_usage: dict[str, int] | None = None
        self._loaded = None  # the model and its tokenizer, once a request has loaded them

    @property
    def version(self) -> str:
        """The versions of mlx-lm, MLX, and transformers, which renders the chat template."""
        try:
            return ", ".join(
                f"{package} {metadata.version(package)}"
                for package in ("mlx-lm", "mlx", "transformers")
            )
        except metadata.PackageNotFoundError as e:
            raise ProviderError(f"mlx-lm is not installed: {INSTALL}") from e

    def complete(self, shared: str, task: str) -> str:
        """One generation; the reply text. Raises ProviderError on any failure.

        The whole request, `shared`, a blank line, then `task`, is one user
        message: a local model gets no prompt caching.
        """
        try:
            import mlx.core as mx
            import mlx_lm
            from mlx_lm.sample_utils import make_sampler
        except ImportError as e:
            raise ProviderError(f"mlx-lm is not installed: {INSTALL}") from e
        model, tokenizer = self._load(mlx_lm)
        try:
            prompt = tokenizer.apply_chat_template(
                [{"role": "user", "content": f"{shared}\n\n{task}"}],
                add_generation_prompt=True,
                tokenize=False,
                reasoning_effort=self.settings["effort"],
            )
            tokens, last = [], None
            for last in mlx_lm.stream_generate(
                model,
                tokenizer,
                tokenizer.encode(prompt, add_special_tokens=False),
                max_tokens=self.max_tokens,
                sampler=make_sampler(temp=TEMPERATURE, top_p=TOP_P),
            ):
                tokens.append(last.token)
            # Special tokens kept: they mark where each channel starts and ends.
            raw = tokenizer.decode(tokens, skip_special_tokens=False)
        except Exception as e:  # any failure of the runtime, such as running out of memory
            raise ProviderError(f"the local model failed: {e}") from e
        finally:
            mx.clear_cache()  # give the memory the reply used back to the builds
        if last is None:
            raise ProviderError("the local model wrote nothing")
        self.last_usage = {
            "input_tokens": last.prompt_tokens,
            "output_tokens": last.generation_tokens,  # reasoning included
        }
        return _answer(raw)

    def _load(self, mlx_lm):
        """The model and its tokenizer, loaded by the first request."""
        if self._loaded is None:
            model, revision = self.settings["model"], self.settings["revision"]
            print(f"Loading the local model, {model}", file=sys.stderr)
            try:
                self._loaded = mlx_lm.load(_downloaded(model, revision) or model, revision=revision)
            except Exception as e:
                raise ProviderError(f"could not load {model}: {e}") from e
        return self._loaded


def _downloaded(model: str, revision: str) -> str | None:
    """The folder holding that snapshot's weights if they are already downloaded,
    so that loading them needs no network; else None, and mlx-lm downloads them."""
    from huggingface_hub import snapshot_download
    from mlx_lm.utils import DEFAULT_ALLOW_PATTERNS

    try:
        return snapshot_download(
            model, revision=revision, local_files_only=True, allow_patterns=DEFAULT_ALLOW_PATTERNS
        )
    except OSError:  # not downloaded, or only in part
        return None


def _answer(raw: str) -> str:
    """The text of gpt-oss's final channel: its answer.

    A reply with channels but no answer, cut off at the limit while reasoning or
    ended on a tool call, is the model's own reply with no answer in it: empty,
    so the screen rejects it as holding no usable test. Text with no channel at
    all was not read as gpt-oss writes, so it stops the run instead of being judged.
    """
    if _FINAL in raw:
        reply = raw.rpartition(_FINAL)[2]
        for end in _ENDS:
            reply = reply.split(end)[0]
        return reply
    if _CHANNEL in raw:
        return ""
    raise ProviderError(f"the local model's reply is not in gpt-oss's format: {raw[-2000:]!r}")

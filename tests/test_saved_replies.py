import pytest

from mutagate.providers.base import ProviderError, SavedReplies, load
from mutagate.providers.claude_code import ClaudeCodeProvider
from mutagate.providers.offline import OfflineProvider


class FakeProvider:
    name = "fake"
    version = "1"

    def __init__(self, settings=None, fail=False):
        self.settings = settings or {"model": "m"}
        self.fail = fail
        self.prompts = []
        self.last_usage = None

    def complete(self, prompt):
        if self.fail:
            raise ProviderError("unreachable")
        self.prompts.append(prompt)
        self.last_usage = {"output_tokens": len(self.prompts)}
        return f"reply {len(self.prompts)}"


def test_a_reply_is_saved_and_reused(tmp_path):
    first = SavedReplies(FakeProvider(), tmp_path)
    assert first.complete("p") == "reply 1"
    assert first.last_usage == {"output_tokens": 1}

    again = SavedReplies(FakeProvider(), tmp_path)
    assert again.complete("p") == "reply 1"
    assert again.provider.prompts == []  # not sent again
    (exchange,) = again.exchanges
    assert exchange.reused and exchange.tokens == {"output_tokens": 1}
    assert [e.reused for e in first.exchanges] == [False]


def test_a_different_prompt_or_settings_is_sent(tmp_path):
    SavedReplies(FakeProvider(), tmp_path).complete("p")

    other_prompt = SavedReplies(FakeProvider(), tmp_path)
    other_prompt.complete("q")
    other_settings = SavedReplies(FakeProvider({"model": "m", "effort": "high"}), tmp_path)
    other_settings.complete("p")

    assert other_prompt.provider.prompts == ["q"]
    assert other_settings.provider.prompts == ["p"]


def test_a_failed_request_saves_nothing(tmp_path):
    with pytest.raises(ProviderError):
        SavedReplies(FakeProvider(fail=True), tmp_path / "replies").complete("p")
    assert not (tmp_path / "replies").exists()


def test_an_unreadable_saved_reply_stops_the_run(tmp_path):
    replies = SavedReplies(FakeProvider(), tmp_path)
    replies.complete("p")
    (saved,) = tmp_path.glob("*.json")
    saved.write_text("not json")
    with pytest.raises(ProviderError):
        SavedReplies(FakeProvider(), tmp_path).complete("p")


def test_load():
    assert isinstance(load("claude-code"), ClaudeCodeProvider)
    assert isinstance(load("offline"), OfflineProvider)
    with pytest.raises(ValueError):
        load("nobody")

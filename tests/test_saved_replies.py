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
        self.requests = []
        self.last_usage = None

    def complete(self, shared, task):
        if self.fail:
            raise ProviderError("unreachable")
        self.requests.append((shared, task))
        self.last_usage = {"output_tokens": len(self.requests)}
        return f"reply {len(self.requests)}"


def test_a_reply_is_saved_and_reused(tmp_path):
    first = SavedReplies(FakeProvider(), tmp_path)
    assert first.complete("s", "t") == "reply 1"
    assert first.provider.requests == [("s", "t")]
    assert first.last_usage == {"output_tokens": 1}

    again = SavedReplies(FakeProvider(), tmp_path)
    assert again.complete("s", "t") == "reply 1"
    assert again.provider.requests == []  # not sent again
    (exchange,) = again.exchanges
    assert exchange.reused and exchange.tokens == {"output_tokens": 1}
    assert [e.reused for e in first.exchanges] == [False]


@pytest.mark.parametrize(
    "shared, task, settings",
    [
        ("rules\n\nclass", "other task", None),
        ("rules\n\nother class", "task", None),
        ("rules", "class\n\ntask", None),
        ("rules\n\nclass", "task", {"model": "m", "effort": "high"}),
    ],
    ids=["other task", "other shared part", "same text split another way", "other settings"],
)
def test_another_request_is_sent(tmp_path, shared, task, settings):
    SavedReplies(FakeProvider(), tmp_path).complete("rules\n\nclass", "task")

    other = FakeProvider(settings)
    SavedReplies(other, tmp_path).complete(shared, task)
    assert other.requests == [(shared, task)]


def test_a_failed_request_saves_nothing(tmp_path):
    with pytest.raises(ProviderError):
        SavedReplies(FakeProvider(fail=True), tmp_path / "replies").complete("s", "t")
    assert not (tmp_path / "replies").exists()


def test_an_unreadable_saved_reply_stops_the_run(tmp_path):
    replies = SavedReplies(FakeProvider(), tmp_path)
    replies.complete("s", "t")
    (saved,) = tmp_path.glob("*.json")
    saved.write_text("not json")
    with pytest.raises(ProviderError):
        SavedReplies(FakeProvider(), tmp_path).complete("s", "t")


def test_load():
    assert isinstance(load("claude-code"), ClaudeCodeProvider)
    assert isinstance(load("offline"), OfflineProvider)
    with pytest.raises(ValueError):
        load("nobody")

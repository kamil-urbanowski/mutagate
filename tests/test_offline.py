import pytest

from mutagate.providers import offline
from mutagate.providers.base import ProviderError
from mutagate.providers.offline import PROMPT_FILE, RESPONSE_FILE, OfflineProvider


def answer_on_wait(monkeypatch, exchange, *replies):
    """Each wait for the response saves the next reply, as the person would."""
    pending = list(replies)

    def sleep(seconds):
        (exchange / RESPONSE_FILE).write_text(pending.pop(0), encoding="utf-8")

    monkeypatch.setattr(offline.time, "sleep", sleep)


def test_round_trip(tmp_path, monkeypatch, capsys):
    exchange = tmp_path / "exchange"
    answer_on_wait(monkeypatch, exchange, "the reply")

    assert OfflineProvider(exchange).complete("the prompt") == "the reply"
    assert (exchange / PROMPT_FILE).read_text(encoding="utf-8") == "the prompt"
    assert not (exchange / RESPONSE_FILE).exists()
    assert str(exchange / RESPONSE_FILE) in capsys.readouterr().err


def test_leftover_response_is_not_taken_as_the_reply(tmp_path, monkeypatch):
    exchange = tmp_path / "exchange"
    exchange.mkdir()
    (exchange / RESPONSE_FILE).write_text("stale", encoding="utf-8")
    answer_on_wait(monkeypatch, exchange, "fresh")

    assert OfflineProvider(exchange).complete("the prompt") == "fresh"


def test_empty_response_counts_as_not_saved_yet(tmp_path, monkeypatch):
    exchange = tmp_path / "exchange"
    answer_on_wait(monkeypatch, exchange, "", "the reply")

    assert OfflineProvider(exchange).complete("the prompt") == "the reply"


def test_exchange_failure_is_a_provider_error(tmp_path):
    blocked = tmp_path / "file"
    blocked.write_text("not a directory")
    with pytest.raises(ProviderError, match="offline exchange"):
        OfflineProvider(blocked / "exchange").complete("the prompt")

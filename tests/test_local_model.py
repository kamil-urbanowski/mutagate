"""The real local model answers through the provider. Needs mlx-lm, the model's
12 GB of weights, and about 13 GB of free memory; under a minute once loaded.
Opt in with MUTAGATE_LOCAL_MODEL=1."""

import pytest

from mutagate.providers.local import LocalProvider

pytestmark = pytest.mark.local_model


def test_the_local_model_answers_with_its_final_channel_only():
    provider = LocalProvider()
    reply = provider.complete(
        "Answer with one Java code block and nothing else.",
        "Write a Java method `int twice(int x)` that returns twice its argument.",
    )
    assert "```" in reply and "twice" in reply
    assert "<|" not in reply  # no channel or end markers
    assert provider.last_usage["input_tokens"] > 0
    assert 0 < provider.last_usage["output_tokens"] <= int(provider.settings["max_tokens"])

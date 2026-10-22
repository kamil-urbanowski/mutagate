"""Shared setup for the integration tests: the opt-in switch and the repo they run on."""

import json
import os
import subprocess
from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[1]
GAMEZONE_URL = "https://github.com/adbautistar/GameZoneUnicesar.git"


def pytest_collection_modifyitems(config, items):
    # Real builds and PIT runs need git, Maven, and JDK 21, and take minutes.
    if os.environ.get("MUTAGATE_INTEGRATION") == "1":
        return
    skip = pytest.mark.skip(reason="opt in with MUTAGATE_INTEGRATION=1")
    for item in items:
        if item.get_closest_marker("integration"):
            item.add_marker(skip)


@pytest.fixture(scope="session")
def corpus() -> Path:
    return ROOT / "corpus"


@pytest.fixture(scope="session")
def gate_fixtures() -> Path:
    """Hand-written candidates for CategoryDiscount; each file names its expected verdict."""
    return Path(__file__).parent / "fixtures" / "gate"


@pytest.fixture(scope="session")
def gamezone_repo(corpus) -> Path:
    """GameZoneUnicesar at its baseline commit, cloned into `work/` on first use.

    An existing clone is used only if it is at that commit with no local
    changes, so the tests always run on the code the baseline measured.
    """
    ledger = json.loads((corpus / "GameZoneUnicesar" / "baseline.json").read_text())
    commit = ledger["repo_commit"]
    repo = ROOT / "work" / "GameZoneUnicesar"
    if not repo.is_dir():
        repo.parent.mkdir(parents=True, exist_ok=True)
        _git("clone", "--quiet", GAMEZONE_URL, str(repo))
        _git("-C", str(repo), "checkout", "--quiet", commit)
    head = _git("-C", str(repo), "rev-parse", "HEAD").strip()
    assert head == commit, f"{repo} is at {head[:7]}; its baseline was measured at {commit[:7]}"
    assert not _git("-C", str(repo), "status", "--porcelain"), f"{repo} has local changes"
    return repo


def _git(*args: str) -> str:
    return subprocess.run(["git", *args], capture_output=True, text=True, check=True).stdout

import pytest
import requests

BASE_URL = "http://127.0.0.1:5000"

@pytest.fixture(scope="module")
def base_url():
    return BASE_URL

def test_get_all_modules(base_url):
    response = requests.get(f"{base_url}/modules")
    assert response.status_code == 200
    data = response.json()
    assert "modules" in data
    assert len(data["modules"]) == 5

def test_filter_by_type(base_url):
    response = requests.get(f"{base_url}/modules", params={"type": "Type 1"})
    assert response.status_code == 200
    data = response.json()
    assert all(module["type"] == "Type 1" for module in data["modules"])

def test_filter_by_status(base_url):
    response = requests.get(f"{base_url}/modules", params={"status": "Active"})
    assert response.status_code == 200
    data = response.json()
    assert all(module["status"] == "Active" for module in data["modules"])

def test_pagination(base_url):
    response = requests.get(f"{base_url}/modules", params={"page": 1, "page_size": 2})
    assert response.status_code == 200
    data = response.json()
    assert len(data["modules"]) == 2
    assert data["page"] == 1
    assert data["page_size"] == 2

def test_combination_filters_pagination(base_url):
    response = requests.get(f"{base_url}/modules", params={"type": "Type 1", "status": "Active", "page": 1, "page_size": 1})
    assert response.status_code == 200
    data = response.json()
    assert len(data["modules"]) == 1
    assert data["modules"][0]["type"] == "Type 1"
    assert data["modules"][0]["status"] == "Active"

def test_invalid_page_number(base_url):
    response = requests.get(f"{base_url}/modules", params={"page": -1})
    assert response.status_code == 200
    data = response.json()
    assert len(data["modules"]) == 0

def test_invalid_page_size(base_url):
    response = requests.get(f"{base_url}/modules", params={"page_size": -10})
    assert response.status_code == 200
    data = response.json()
    assert len(data["modules"]) == 5

def test_nonexistent_filter_value(base_url):
    response = requests.get(f"{base_url}/modules", params={"type": "Nonexistent"})
    assert response.status_code == 200
    data = response.json()
    assert len(data["modules"]) == 0

"""
HTTP Client wrapper using Requests to interact with the Jakarta REST API.
"""
import requests
from requests.auth import HTTPBasicAuth
from config import DEFAULT_TIMEOUT

class TranslationApiClient:
    def __init__(self, api_url: str, username: str, password: str, timeout: int = DEFAULT_TIMEOUT):
        self.api_url = api_url
        self.username = username
        self.password = password
        self.timeout = timeout

    def translate(self, text: str) -> dict:
        """
        Sends translation request to the REST API.
        Returns response dict containing 'translatedText', 'originalText', etc.
        Raises RuntimeError on API or connection errors.
        """
        if not text or not text.strip():
            raise ValueError("Text to translate cannot be empty.")

        payload = {"text": text.strip()}
        auth = HTTPBasicAuth(self.username, self.password)
        headers = {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }

        try:
            response = requests.post(
                self.api_url,
                json=payload,
                auth=auth,
                headers=headers,
                timeout=self.timeout
            )

            try:
                data = response.json()
            except ValueError:
                data = None

            if response.status_code == 200:
                if data and "translatedText" in data:
                    return data
                raise RuntimeError("Unexpected server response format.")
            elif response.status_code == 401:
                raise RuntimeError("401 Unauthorized: Invalid username or password.")
            elif response.status_code == 400:
                err_msg = data.get("error") if data else "Bad request."
                raise RuntimeError(f"400 Bad Request: {err_msg}")
            else:
                err_msg = data.get("error") if data else f"HTTP {response.status_code}"
                raise RuntimeError(f"Server error ({response.status_code}): {err_msg}")

        except requests.exceptions.Timeout:
            raise RuntimeError("Request timed out. Please check if the server is running.")
        except requests.exceptions.ConnectionError:
            raise RuntimeError(f"Could not connect to server at {self.api_url}.")
        except Exception as e:
            if isinstance(e, (ValueError, RuntimeError)):
                raise
            raise RuntimeError(f"Unexpected error: {str(e)}")

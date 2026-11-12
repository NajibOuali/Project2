"""
Configuration settings for Python Darija Translator Client.
"""
import os

DEFAULT_API_URL = os.getenv("TRANSLATOR_API_URL", "http://localhost:8080/backend/api/translator/translate")
DEFAULT_USERNAME = os.getenv("TRANSLATOR_API_USER", "user")
DEFAULT_PASSWORD = os.getenv("TRANSLATOR_API_PASS", "darija123")
DEFAULT_TIMEOUT = 15

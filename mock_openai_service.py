#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Mock OpenAI Service for Testing
This is a test script - do not use in production
"""

import requests
import json
import os
from typing import Dict, Any, Optional

class MockOpenAIService:
    """Mock OpenAI Service for testing functionality"""
    
    def __init__(self):
        # Test API Key - DO NOT USE IN PRODUCTION
        self.api_key = "sk-4a79aae22608494a86e33921b7e1f4c5"
        self.base_url = "https://api.openai.com/v1"
        self.headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
    
    def test_chat_completion(self, prompt: str, model: str = "gpt-4") -> Dict[str, Any]:
        """Test OpenAI Chat Completion API"""
        try:
            url = f"{self.base_url}/chat/completions"
            
            payload = {
                "model": model,
                "messages": [
                    {"role": "user", "content": prompt}
                ],
                "max_tokens": 1000,
                "temperature": 0.7,
                "top_p": 1.0,
                "frequency_penalty": 0.0,
                "presence_penalty": 0.0
            }
            
            response = requests.post(url, headers=self.headers, json=payload, timeout=30)
            response.raise_for_status()
            
            return response.json()
            
        except requests.exceptions.RequestException as e:
            return {"error": f"Request failed: {str(e)}"}
        except Exception as e:
            return {"error": f"Unexpected error: {str(e)}"}
    
    def test_image_generation(self, prompt: str, model: str = "dall-e-3") -> Dict[str, Any]:
        """Test OpenAI Image Generation API"""
        try:
            url = f"{self.base_url}/images/generations"
            
            payload = {
                "model": model,
                "prompt": prompt,
                "size": "1024x1024",
                "quality": "standard",
                "n": 1
            }
            
            response = requests.post(url, headers=self.headers, json=payload, timeout=60)
            response.raise_for_status()
            
            return response.json()
            
        except requests.exceptions.RequestException as e:
            return {"error": f"Request failed: {str(e)}"}
        except Exception as e:
            return {"error": f"Unexpected error: {str(e)}"}
    
    def test_models_list(self) -> Dict[str, Any]:
        """Test OpenAI Models API to verify API key"""
        try:
            url = f"{self.base_url}/models"
            
            response = requests.get(url, headers=self.headers, timeout=30)
            response.raise_for_status()
            
            return response.json()
            
        except requests.exceptions.RequestException as e:
            return {"error": f"Request failed: {str(e)}"}
        except Exception as e:
            return {"error": f"Unexpected error: {str(e)}"}
    
    def test_api_key_validity(self) -> bool:
        """Test if API key is valid"""
        try:
            result = self.test_models_list()
            return "error" not in result
        except Exception:
            return False
    
    def test_embeddings(self, text: str, model: str = "text-embedding-ada-002") -> Dict[str, Any]:
        """Test OpenAI Embeddings API"""
        try:
            url = f"{self.base_url}/embeddings"
            
            payload = {
                "model": model,
                "input": text
            }
            
            response = requests.post(url, headers=self.headers, json=payload, timeout=30)
            response.raise_for_status()
            
            return response.json()
            
        except requests.exceptions.RequestException as e:
            return {"error": f"Request failed: {str(e)}"}
        except Exception as e:
            return {"error": f"Unexpected error: {str(e)}"}


def main():
    """Main function for testing"""
    print("=== OpenAI API Testing Script ===")
    print("WARNING: This script contains API keys - use only for testing!")
    print()
    
    service = MockOpenAIService()
    
    # Test API key validity
    print("1. Testing API Key Validity...")
    is_valid = service.test_api_key_validity()
    print(f"   API Key Valid: {is_valid}")
    print()
    
    if not is_valid:
        print("❌ API Key is invalid. Please check your key.")
        return
    
    # Test models list
    print("2. Testing Models List...")
    models_result = service.test_models_list()
    if "error" not in models_result:
        print(f"   ✅ Successfully retrieved {len(models_result.get('data', []))} models")
    else:
        print(f"   ❌ Error: {models_result['error']}")
    print()
    
    # Test chat completion
    print("3. Testing Chat Completion...")
    chat_result = service.test_chat_completion("Hello, how are you? Please respond briefly.")
    if "error" not in chat_result:
        content = chat_result.get('choices', [{}])[0].get('message', {}).get('content', '')
        print(f"   ✅ Chat Response: {content[:100]}...")
    else:
        print(f"   ❌ Error: {chat_result['error']}")
    print()
    
    # Test embeddings
    print("4. Testing Embeddings...")
    embed_result = service.test_embeddings("This is a test sentence for embeddings.")
    if "error" not in embed_result:
        embedding_length = len(embed_result.get('data', [{}])[0].get('embedding', []))
        print(f"   ✅ Successfully generated embedding with {embedding_length} dimensions")
    else:
        print(f"   ❌ Error: {embed_result['error']}")
    print()
    
    # Test image generation (optional - costs money)
    print("5. Testing Image Generation (optional)...")
    user_input = input("   Do you want to test image generation? (y/n): ").lower().strip()
    if user_input == 'y':
        image_result = service.test_image_generation("A beautiful sunset over mountains")
        if "error" not in image_result:
            image_url = image_result.get('data', [{}])[0].get('url', '')
            print(f"   ✅ Image generated successfully: {image_url}")
        else:
            print(f"   ❌ Error: {image_result['error']}")
    else:
        print("   ⏭️  Skipped image generation test")
    
    print()
    print("=== Testing Complete ===")


if __name__ == "__main__":
    main()

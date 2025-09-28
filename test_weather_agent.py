#!/usr/bin/env python3
"""
A2A Weather Agent Test Script (Python)
Tests all weather agent endpoints and methods with detailed output
"""

import requests
import json
import sys
import time
from typing import Dict, Any, Optional

class WeatherAgentTester:
    def __init__(self, agent_url: str):
        self.agent_url = agent_url.rstrip('/')
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'A2A-Weather-Agent-Tester/1.0',
            'Content-Type': 'application/json'
        })
        
    def print_status(self, message: str, status: str = "INFO"):
        colors = {
            "INFO": "\033[0;34m",
            "SUCCESS": "\033[0;32m", 
            "WARNING": "\033[1;33m",
            "ERROR": "\033[0;31m"
        }
        color = colors.get(status, colors["INFO"])
        print(f"{color}[{status}]{'\033[0m'} {message}")
        
    def make_request(self, method: str, url: str, data: Optional[Dict] = None) -> Optional[Dict]:
        """Make HTTP request and return JSON response"""
        try:
            if method.upper() == "GET":
                response = self.session.get(url, timeout=10)
            elif method.upper() == "POST":
                response = self.session.post(url, json=data, timeout=10)
            else:
                raise ValueError(f"Unsupported method: {method}")
                
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            self.print_status(f"Request failed: {e}", "ERROR")
            return None
        except json.JSONDecodeError as e:
            self.print_status(f"JSON decode failed: {e}", "ERROR")
            return None
            
    def test_root_endpoint(self) -> bool:
        """Test root endpoint"""
        self.print_status("Testing root endpoint...")
        response = self.make_request("GET", f"{self.agent_url}/")
        
        if response:
            self.print_status("Root endpoint working", "SUCCESS")
            print(f"  Name: {response.get('name', 'N/A')}")
            print(f"  Version: {response.get('version', 'N/A')}")
            print(f"  Status: {response.get('status', 'N/A')}")
            return True
        else:
            self.print_status("Root endpoint failed", "ERROR")
            return False
            
    def test_agent_card(self) -> bool:
        """Test agent card endpoint"""
        self.print_status("Testing agent card...")
        response = self.make_request("GET", f"{self.agent_url}/agent")
        
        if response:
            self.print_status("Agent card working", "SUCCESS")
            print(f"  Name: {response.get('name', 'N/A')}")
            print(f"  Version: {response.get('version', 'N/A')}")
            print(f"  Protocol: {response.get('protocolVersion', 'N/A')}")
            skills = response.get('skills', [])
            print(f"  Skills: {len(skills)} available")
            return True
        else:
            self.print_status("Agent card failed", "ERROR")
            return False
            
    def test_health_check(self) -> bool:
        """Test health check endpoint"""
        self.print_status("Testing health check...")
        response = self.make_request("GET", f"{self.agent_url}/agent/health")
        
        if response:
            self.print_status("Health check working", "SUCCESS")
            print(f"  Status: {response.get('status', 'N/A')}")
            print(f"  Platform: {response.get('platform', 'N/A')}")
            print(f"  Weather Mode: {response.get('weatherMode', 'N/A')}")
            return True
        else:
            self.print_status("Health check failed", "ERROR")
            return False
            
    def test_jsonrpc_method(self, method: str, params: Dict, test_name: str) -> bool:
        """Test a JSON-RPC method"""
        self.print_status(f"Testing {test_name}...")
        
        data = {
            "jsonrpc": "2.0",
            "method": method,
            "params": params,
            "id": int(time.time())
        }
        
        response = self.make_request("POST", f"{self.agent_url}/jsonrpc", data)
        
        if response and 'result' in response:
            self.print_status(f"{test_name} working", "SUCCESS")
            result = response['result']
            
            # Print relevant result fields
            if 'location' in result:
                print(f"  Location: {result['location']}")
            if 'temperature' in result:
                print(f"  Temperature: {result['temperature']}°F")
            if 'condition' in result:
                print(f"  Condition: {result['condition']}")
            if 'data_source' in result:
                print(f"  Data Source: {result['data_source']}")
            if 'forecast_days' in result:
                print(f"  Forecast Days: {result['forecast_days']}")
            if 'alert_count' in result:
                print(f"  Alert Count: {result['alert_count']}")
            if 'data_points' in result:
                print(f"  Data Points: {result['data_points']}")
                
            return True
        elif response and 'error' in response:
            self.print_status(f"{test_name} returned error: {response['error']}", "WARNING")
            return False
        else:
            self.print_status(f"{test_name} failed", "ERROR")
            return False
            
    def test_weather_methods(self) -> bool:
        """Test all weather methods"""
        self.print_status("Testing Weather Methods...")
        
        tests = [
            {
                "method": "get_current_weather",
                "params": {"zip_code": "10001", "country_code": "us"},
                "name": "Current Weather"
            },
            {
                "method": "get_weather_forecast", 
                "params": {"zip_code": "90210", "country_code": "us"},
                "name": "Weather Forecast"
            },
            {
                "method": "get_weather_alerts",
                "params": {"zip_code": "60601", "country_code": "us"},
                "name": "Weather Alerts"
            },
            {
                "method": "get_weather_history",
                "params": {
                    "zip_code": "33101", 
                    "country_code": "us",
                    "start_date": "2024-01-01",
                    "end_date": "2024-01-07"
                },
                "name": "Weather History"
            }
        ]
        
        success_count = 0
        for test in tests:
            if self.test_jsonrpc_method(test["method"], test["params"], test["name"]):
                success_count += 1
            print()  # Add spacing between tests
            
        return success_count == len(tests)
        
    def test_a2a_protocol_methods(self) -> bool:
        """Test A2A protocol methods"""
        self.print_status("Testing A2A Protocol Methods...")
        
        tests = [
            {
                "method": "agent.discover",
                "params": {},
                "name": "Agent Discover"
            },
            {
                "method": "agent.getSkills",
                "params": {},
                "name": "Agent Skills"
            },
            {
                "method": "agent.info",
                "params": {},
                "name": "Agent Info"
            },
            {
                "method": "agent.getCapabilities",
                "params": {},
                "name": "Agent Capabilities"
            }
        ]
        
        success_count = 0
        for test in tests:
            if self.test_jsonrpc_method(test["method"], test["params"], test["name"]):
                success_count += 1
            print()  # Add spacing between tests
            
        return success_count == len(tests)
        
    def test_error_handling(self) -> bool:
        """Test error handling"""
        self.print_status("Testing Error Handling...")
        
        # Test invalid method
        self.print_status("Testing invalid method...")
        data = {
            "jsonrpc": "2.0",
            "method": "invalid_method",
            "params": {},
            "id": 999
        }
        response = self.make_request("POST", f"{self.agent_url}/jsonrpc", data)
        
        if response and 'error' in response:
            self.print_status("Invalid method handling working", "SUCCESS")
            print(f"  Error Code: {response['error'].get('code', 'N/A')}")
            print(f"  Error Message: {response['error'].get('message', 'N/A')}")
        else:
            self.print_status("Invalid method handling failed", "ERROR")
            return False
            
        print()
        
        # Test missing required parameter
        self.print_status("Testing missing zip_code parameter...")
        data = {
            "jsonrpc": "2.0",
            "method": "get_current_weather",
            "params": {"country_code": "us"},
            "id": 998
        }
        response = self.make_request("POST", f"{self.agent_url}/jsonrpc", data)
        
        if response and 'result' in response and 'error' in response['result']:
            self.print_status("Missing parameter handling working", "SUCCESS")
            print(f"  Error: {response['result'].get('error', 'N/A')}")
            print(f"  Error Code: {response['result'].get('error_code', 'N/A')}")
        else:
            self.print_status("Missing parameter handling failed", "ERROR")
            return False
            
        return True
        
    def run_all_tests(self) -> bool:
        """Run all tests"""
        self.print_status(f"Testing A2A Weather Agent at: {self.agent_url}")
        self.print_status("=" * 60)
        
        tests = [
            ("Root Endpoint", self.test_root_endpoint),
            ("Agent Card", self.test_agent_card),
            ("Health Check", self.test_health_check),
            ("Weather Methods", self.test_weather_methods),
            ("A2A Protocol Methods", self.test_a2a_protocol_methods),
            ("Error Handling", self.test_error_handling)
        ]
        
        passed = 0
        total = len(tests)
        
        for test_name, test_func in tests:
            print()
            self.print_status(f"Running {test_name}...")
            if test_func():
                passed += 1
                self.print_status(f"{test_name} PASSED", "SUCCESS")
            else:
                self.print_status(f"{test_name} FAILED", "ERROR")
            print("-" * 40)
            
        # Summary
        print()
        self.print_status("=" * 60)
        self.print_status(f"Test Results: {passed}/{total} tests passed")
        
        if passed == total:
            self.print_status("All tests passed! 🎉", "SUCCESS")
            return True
        else:
            self.print_status(f"{total - passed} tests failed", "ERROR")
            return False

def main():
    if len(sys.argv) != 2:
        print("Usage: python3 test_weather_agent.py <agent_url>")
        print("Example: python3 test_weather_agent.py https://your-weather-agent.herokuapp.com")
        sys.exit(1)
        
    agent_url = sys.argv[1]
    tester = WeatherAgentTester(agent_url)
    
    success = tester.run_all_tests()
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()

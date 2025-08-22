const https = require('https');
const http = require('http');

// Disable SSL verification for local testing
process.env["NODE_TLS_REJECT_UNAUTHORIZED"] = 0;

const baseURL = 'http://localhost:8080/api';

// Function to make HTTP requests
function makeRequest(options, data = null) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', (chunk) => body += chunk);
      res.on('end', () => {
        try {
          const parsed = JSON.parse(body);
          resolve({ status: res.statusCode, data: parsed, headers: res.headers });
        } catch (e) {
          resolve({ status: res.statusCode, data: body, headers: res.headers });
        }
      });
    });
    
    req.on('error', reject);
    
    if (data) {
      req.write(JSON.stringify(data));
    }
    req.end();
  });
}

async function testAdminAPI() {
  try {
    console.log('0. Creating test user...');
    
    // Step 0: Create test user
    const signupOptions = {
      hostname: 'localhost',
      port: 8080,
      path: '/api/auth/signup',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    };
    
    const signupData = {
      email: 'test@test.com',
      password: 'Test123'
    };
    
    const signupResponse = await makeRequest(signupOptions, signupData);
    console.log('Signup Status:', signupResponse.status);
    
    console.log('\n1. Testing login...');
    
    // Step 1: Login
    const loginOptions = {
      hostname: 'localhost',
      port: 8080,
      path: '/api/auth/login',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    };
    
    const loginData = {
      email: 'test@test.com',
      password: 'Test123' // Updated to match user's password
    };
    
    const loginResponse = await makeRequest(loginOptions, loginData);
    console.log('Login Response Status:', loginResponse.status);
    console.log('Login Response:', loginResponse.data);
    
    if (loginResponse.status !== 200) {
      console.log('Login failed, cannot test admin endpoints');
      return;
    }
    
    const token = loginResponse.data.token;
    console.log('Token obtained:', token ? 'Yes' : 'No');
    
    // Step 2: Test user info
    console.log('\n2. User info from login:', {
      userId: loginResponse.data.userId,
      email: loginResponse.data.email,
      isSuperadmin: loginResponse.data.isSuperadmin
    });
    
    // Step 3: Test products endpoint
    console.log('\n3. Testing products endpoint...');
    
    const productsOptions = {
      hostname: 'localhost',
      port: 8080,
      path: '/api/products',
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    };
    
    const productsResponse = await makeRequest(productsOptions);
    console.log('Products Status:', productsResponse.status);
    console.log('Products Response:', JSON.stringify(productsResponse.data, null, 2));
    
    // Step 4: Test product modules endpoint
    if (productsResponse.data && productsResponse.data.length > 0) {
      const productId = productsResponse.data[0].productId;
      console.log(`\n4. Testing product modules endpoint for product ID: ${productId}...`);
      
      const modulesOptions = {
        hostname: 'localhost',
        port: 8080,
        path: `/api/products/${productId}/modules`,
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      };
      
      const modulesResponse = await makeRequest(modulesOptions);
      console.log('Modules Status:', modulesResponse.status);
      console.log('Modules Response:', JSON.stringify(modulesResponse.data, null, 2));
    }
    
  } catch (error) {
    console.error('Error testing admin API:', error);
  }
}

testAdminAPI();
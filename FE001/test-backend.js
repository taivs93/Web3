// Test script để kiểm tra backend
const testBackend = async () => {
  console.log('Testing backend connection...')
  
  try {
    // Test health endpoint
    console.log('1. Testing health endpoint...')
    const healthResponse = await fetch('http://localhost:8080/health')
    console.log('Health status:', healthResponse.status)
    
    if (healthResponse.ok) {
      const healthData = await healthResponse.text()
      console.log('Health response:', healthData)
    } else {
      console.error('Health check failed:', healthResponse.status)
    }
    
    // Test gas endpoint
    console.log('2. Testing gas endpoint...')
    const gasResponse = await fetch('http://localhost:8080/gas/estimate/bsc')
    console.log('Gas status:', gasResponse.status)
    
    if (gasResponse.ok) {
      const gasData = await gasResponse.json()
      console.log('Gas response:', gasData)
    } else {
      console.error('Gas API failed:', gasResponse.status)
    }
    
    // Test with CORS headers
    console.log('3. Testing with CORS headers...')
    const corsResponse = await fetch('http://localhost:8080/health', {
      method: 'GET',
      headers: {
        'Origin': 'http://localhost:5173',
        'Access-Control-Request-Method': 'GET',
        'Access-Control-Request-Headers': 'Content-Type'
      }
    })
    console.log('CORS test status:', corsResponse.status)
    console.log('CORS headers:', corsResponse.headers.get('Access-Control-Allow-Origin'))
    
  } catch (error) {
    console.error('Backend test failed:', error)
  }
}

// Run test
testBackend()

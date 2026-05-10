#!/bin/bash
set -e

# Navigate to the project root
cd "$(dirname "$0")/.."
PROJECT_ROOT=$(pwd)

echo "=== University Management System ==="
echo "[1/3] Building backend JAR..."
bash scripts/build.sh

echo "[2/3] Checking frontend dependencies..."
cd "$PROJECT_ROOT/frontend"
if [ ! -d "node_modules" ]; then
    npm install
fi

echo "[3/3] Starting servers..."

# Function to handle shutdown of both servers on exit
cleanup() {
    echo ""
    echo "Shutting down servers..."
    kill $BACKEND_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    exit 0
}

# Trap SIGINT (Ctrl+C) and SIGTERM to clean up gracefully
trap cleanup SIGINT SIGTERM

echo "-> Starting Java REST API on port 8080..."
cd "$PROJECT_ROOT"
java -jar university-system.jar --server 8080 &
BACKEND_PID=$!

# Give the backend a second to start
sleep 1

echo "-> Starting React Frontend on port 5173..."
cd "$PROJECT_ROOT/frontend"
npm run dev &
FRONTEND_PID=$!

echo ""
echo "================================================="
echo "✅ Backend API is running at: http://localhost:8080/api"
echo "✅ Frontend is running at:    http://localhost:5173"
echo "Press Ctrl+C to stop both servers."
echo "================================================="

# Wait for background processes so the script stays alive
wait $BACKEND_PID $FRONTEND_PID

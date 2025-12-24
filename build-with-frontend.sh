#!/bin/sh

set -e

PROJECT_ROOT_DIR="$( cd "$( dirname "$0"  )" && pwd )"

echo "Step 1: Building frontend UI..."
cd $PROJECT_ROOT_DIR/dbswitch-admin-ui
npm install
npm run build

echo "Step 2: Copying frontend static files to backend resources..."
rm -rf $PROJECT_ROOT_DIR/dbswitch-admin/src/main/resources/static/*
cp -r $PROJECT_ROOT_DIR/dbswitch-admin-ui/dist/* $PROJECT_ROOT_DIR/dbswitch-admin/src/main/resources/static/

echo "Step 3: Building backend with Maven..."
cd $PROJECT_ROOT_DIR
mvn clean package -Dmaven.test.skip=true

echo "Build completed successfully!"

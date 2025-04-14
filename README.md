# CSC3055NSProject4_Ashworth_Forsyth_Hamilton

### This project allows a new user to register for an account to read and write messages
### The user will need to connect to a server over TLS and use 2FA to retrieve all posts specific to the user
Jack Ashworth, Owen Forsyth, David Hamilton

# Secure Bulletin Board System

This is a secure client-server bulletin board system built in Java. It uses TLS for encrypted communication, ElGamal for public/private key encryption, SCRYPT for password hashing, and TOTP for two-factor authentication.

## required jar files

bouncycastle.jar	
merrimackutil.jar	

jackson-core.jar	    Core JSON processing (used for Jackson pretty-printing)
jackson-databind.jar	Binds Java objects to JSON and vice versa
jackson-annotations.jar	Used for @JsonProperty annotations on Java fields

## JSON files

- `config.json`   contains configuration for server

The following .json files are created automatically, if not already present
- `users.json`    contains all created users
- `board.json`    represents board, contains all post objects

## 🔐 TLS Configuration

This project uses TLS for secure communication between the client and server.

- `keystore.p12` — Contains the server’s private key and certificate.
- `clienttruststore.p12` — Contains the server's certificate trusted by the client.

These are already included in the repository.

## Setup

### 1. Compile the Project

javac -d out -cp "lib/*" $(find src -name "*.java")

### 2. Run server

java -cp "out:lib/*" server.BulletinBoardService --config config.json

### 3. Run client

java -cp "out:lib/*" client.ClientCLI


## Features

- Account creation with secure password storage (SCrypt)
- ElGamal public/private key generation per user
- TOTP-based 2FA using a Base32 key (compatible with Google Authenticator)
- Posting encrypted messages to other users
- Retrieving and decrypting messages addressed to you
- JSON-based storage (users.json and board.json) with pretty formatting (Jackson)
- TLS/SSL encrypted communication using Java keystores

import React, { useState } from 'react';
import { backend } from './config.json';
import './App.css';

function App() {
  const [output, setOutput] = useState();
  const [email, setEmail] = useState();
  const [username, setUsername] = useState();
  const [password, setPassword] = useState();
  const [verifyKey, setVerifyKey] = useState();
  const [itemId, setItemId] = useState(1);
  const [timestamp, setTimestamp] = useState(Date.now());
  const [limit, setLimit] = useState(25);

  const getItem = id => {
    fetch(`${backend}/item/${id}`)
      .then(r => r.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput(JSON.stringify(json.item, null, 2));
        } else {
          setOutput("Failed: " + json.error);
        }
      })
      .catch(err => setOutput("Failed: " + err))
  }

  const register = (email, username, password) => {
    if (username.length === 0 || password.length === 0 || email.length === 0) {
      setOutput("Please enter an email, username and password.");
      return;
    }
    fetch(`${backend}/adduser`, {
      method: "POST",
      body: JSON.stringify({ email, username, password }),
      headers: [
        ["content-type", "application/json"],
      ],
    })
      .then(json => {
        if (json.status === "OK") {
          setOutput("Registered successfully.");
        } else {
          setOutput("Error: " + json.error);
        }
      })
      .catch(setOutput);
  };

  const verify = (email, key) => {
    if (email.length === 0 || key === 0) {
      setOutput("Please input an email and key.");
      return;
    }
    fetch(`${backend}/verify`, {
      method: "POST",
      body: JSON.stringify({ email, key }),
      headers: [
        ["content-type", "application/json"],
      ],
    })
      .then(json => {
        if (json.status === "OK") {
          setOutput("Verified successfully.");
        } else {
          setOutput("Error: " + json.error);
        }
      })
      .catch(setOutput);
  };
  const resendVerification = (email) => {
    if (email.length === 0) {
      setOutput("Please input an email.");
      return;
    }
    fetch(`${backend}/resendVerification`, {
      method: "POST",
      body: JSON.stringify({ email }),
      headers: [
        ["content-type", "application/json"],
      ],
    })
      .then(json => {
        if (json.status === "OK") {
          setOutput("Email sent again.");
        } else {
          setOutput("Error: " + json.error);
        }
      })
      .catch(setOutput);
  };

  const login = (username, password) => {
    if (username.length === 0 || password.length === 0) {
      setOutput("Please input a username and password.");
      return;
    }
    fetch(`${backend}/login`, {
      method: "POST",
      credentials: "include",
      headers: [
        ["content-type", "application/json"],
      ],
      body: JSON.stringify({ username, password }),
    })
      .then(res => res.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput("Logged in successfully.");
        } else {
          setOutput("Error: " + json.error);
        }
      })
      .catch(setOutput);
  }
  const createDoot = content => {
    fetch(`${backend}/additem`, {
      method: "POST",
      credentials: "include",
      headers: [
        ["content-type", "application/json"],
      ],
      body: JSON.stringify({
        childType: null,
        content
      })
    })
      .then(r => r.json())
      .then(json => {
        if (json.status === "OK") {
          getItem(json.id);
        } else {
          setOutput(json.error);
        }
      })
      .catch(setOutput);
  }

  const doSearch = (ts, lim) => {
    if (ts == 0) {
      ts = null;
    }
    fetch(`${backend}/search`, {
      method: "POST",
      body: JSON.stringify({ timestamp: ts, limit: lim }),
      headers: [
        ["content-type", "application/json"],
      ],
    })
      .then(r => r.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput(JSON.stringify(json.items, null, 2));
        } else {
          setOutput("Failed: " + json.error);
        }
      })
      .catch(err => setOutput("Failed: " + err));
  }

  const logout = () => {
    fetch(`${backend}/logout`, {
      method: "POST",
      credentials: "include",
      headers: [
        ["content-type", "application/json"],
      ],
    })
      .then(res => res.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput("Logged out successfully.");
        } else {
          setOutput("Error: " + json.error);
        }
      })
      .catch(setOutput);
  }

  return (
    <div className="App">
      <header className="App-header">
        <div>
          <label for="email">
            Email:
            <input name="email" type="text" onInput={e => setEmail(e.currentTarget.value)} value={email} />
          </label>
          <label for="verifyKey">
            Key:
            <input name="verifyKey" type="text" onInput={e => setVerifyKey(e.currentTarget.value)} value={verifyKey} />
          </label>
          <br />
          <label for="username">
            Username:
          <input name="username" type="text" onInput={e => setUsername(e.currentTarget.value)} value={username} />
          </label>
          <label for="password">
            Password:
          <input name="password" type="password" onInput={e => setPassword(e.currentTarget.value)} value={password} />
          </label>
        </div>
        <div>
          <button type="submit" onClick={e => login(username, password)} >Log in</button>
          <button type="submit" onClick={e => register(email, username, password)} >Register</button>
          <button type="submit" onClick={e => verify(email, verifyKey)} >Verify</button>
          <button type="submit" onClick={e => resendVerification(email)} >Resend Verification</button>
          <button type="submit" onClick={e => logout()} >Logout</button>
        </div>
        <br /> <br />
        <div>
          <label for="itemID">
            Item ID:
          <input name="itemID" type="number" onInput={e => setItemId(e.currentTarget.valueAsNumber)} value={itemId} />
          </label>
          <button onClick={() => getItem(itemId)}>Get Item</button>
        </div>
        <br />
        <div>
          <label for="timestamp">
            Timestamp:
            <input name="timestamp" type="number" onInput={e => setTimestamp(e.currentTarget.value)} value={timestamp} />
          </label>
          <label for="limit">
            Limit:
            <input name="limit" type="number" onInput={e => setLimit(e.currentTarget.value)} value={limit} />
          </label>
          <br />
          <button onClick={() => doSearch(timestamp, limit)}>Search</button>
        </div>
        <br />
        <textarea value={output} onInput={e => setOutput(e.currentTarget.value)} />
        <br />
        <button onClick={() => createDoot(output)} >Create Doot from above box</button>
      </header>
    </div>
  );
}

export default App;

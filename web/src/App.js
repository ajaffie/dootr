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
  const [timestamp, setTimestamp] = useState((Date.now() / 1000).toFixed(0));
  const [limit, setLimit] = useState(25);
  const [followingToggle, setFollowingToggle] = useState(true);
  const [searchUsername, setSearchUsername] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [followUsername, setFollowUsername] = useState("");
  const [followLimit, setFollowLimit] = useState(50);

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
  const reply = (parent, content) => {
    fetch(`${backend}/additem`, {
      method: "POST",
      credentials: "include",
      headers: [
        ["content-type", "application/json"],
      ],
      body: JSON.stringify({
        childType: "reply",
        content,
        parent,
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
  const retweet = id => {
    fetch(`${backend}/additem`, {
      method: "POST",
      credentials: "include",
      headers: [
        ["content-type", "application/json"],
      ],
      body: JSON.stringify({
        childType: "retweet",
        parent: id,
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
  const deleteDoot = id => {
    fetch(`${backend}/item/${id}`, {
      method: "DELETE",
      credentials: "include",
    }).then(resp => {
      if (resp.ok) {
        setOutput(`Doot ${id} deleted successfully.`);
      } else if (resp.status === 404) {
        setOutput(`Doot does not exist.`);
      } else {
        setOutput(`You do not own doot ${id}.`);
      }
    })
      .catch(setOutput);
  }

  const doSearch = (ts, lim, f, u, q) => {
    if (ts == 0) {
      ts = null;
    }
    fetch(`${backend}/search`, {
      method: "POST",
      body: JSON.stringify({
        timestamp: ts,
        limit: lim,
        following: f,
        q: q.length === 0 ? null : q,
        username: u.length === 0 ? null : u,
      }),
      headers: [
        ["content-type", "application/json"],
      ],
      credentials: "include",
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
  const follow = (u, fOrNot) => {
    if (u.length === 0) {
      setOutput("Please input a username.");
      return;
    }
    fetch(`${backend}/follow`, {
      method: "POST",
      credentials: "include",
      headers: [
        ["content-type", "application/json"],
      ],
      body: JSON.stringify({
        username: u,
        follow: fOrNot,
      })
    })
      .then(r => r.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput(`You are now${!fOrNot ? " not" : ""} following ${u}.`);
        } else {
          setOutput(json.error);
        }
      })
      .catch(setOutput);
  }
  const getProfile = u => {
    if (u.length === 0) {
      setOutput("Please input a username.");
      return;
    }
    fetch(`${backend}/user/${u}`)
      .then(r => r.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput(JSON.stringify(json.user, null, 2));
        } else {
          setOutput("Failed: " + json.error);
        }
      })
      .catch(err => setOutput("Failed: " + err))
  }
  const getPosts = (u, lim) => {
    if (u.length === 0) {
      setOutput("Please input a username.");
      return;
    }
    fetch(`${backend}/user/${u}/posts?limit=${lim}`)
      .then(r => r.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput(JSON.stringify(json.items, null, 2));
        } else {
          setOutput("Failed: " + json.error);
        }
      })
      .catch(err => setOutput("Failed: " + err))
  }
  const getFollowing = (u, lim) => {
    if (u.length === 0) {
      setOutput("Please input a username.");
      return;
    }
    fetch(`${backend}/user/${u}/following?limit=${lim}`)
      .then(r => r.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput(JSON.stringify(json.users, null, 2));
        } else {
          setOutput("Failed: " + json.error);
        }
      })
      .catch(err => setOutput("Failed: " + err))
  }
  const getFollowers = (u, lim) => {
    if (u.length === 0) {
      setOutput("Please input a username.");
      return;
    }
    fetch(`${backend}/user/${u}/followers?limit=${lim}`)
      .then(r => r.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput(JSON.stringify(json.users, null, 2));
        } else {
          setOutput("Failed: " + json.error);
        }
      })
      .catch(err => setOutput("Failed: " + err))
  }

  const like = (id, lOrNot) => {
    if (id.length === 0) {
      setOutput("Please input a username.");
      return;
    }
    fetch(`${backend}/item/${id}/like`, {
      method: "POST",
      credentials: "include",
      headers: [
        ["content-type", "application/json"],
      ],
      body: JSON.stringify({
        like: lOrNot,
      })
    })
      .then(r => r.json())
      .then(json => {
        if (json.status === "OK") {
          setOutput(`You now ${!lOrNot ? "dis" : ""}like ${id}.`);
        } else {
          setOutput(json.error);
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
          <input name="itemID" type="text" onInput={e => setItemId(e.currentTarget.value)} value={itemId} />
          </label>
          <br />
          <button onClick={() => getItem(itemId)}>Get Item</button>
          <button onClick={() => retweet(itemId)}>Retweet Item</button>
          <button onClick={() => deleteDoot(itemId)}>Delete Item</button>
          <br />
          <button onClick={() => like(itemId, true)}>Like Item</button>
          <button onClick={() => like(itemId, false)}>Dislike Item</button>
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
          <label for="followingToggle">
            Following:
            <input type="checkbox" name="followingToggle" onInput={e => setFollowingToggle(e.currentTarget.checked)} defaultChecked={true} />
          </label>
          <label for="searchQuery">
            Query:
            <input name="searchQuery" type="text" onInput={e => setSearchQuery(e.currentTarget.value)} value={searchQuery} />
          </label>
          <label for="searchUsername">
            Username:
            <input name="searchUsername" type="text" onInput={e => setSearchUsername(e.currentTarget.value)} value={searchUsername} />
          </label>

          <br />
          <button onClick={() => doSearch(timestamp, limit, followingToggle, searchUsername, searchQuery)}>Search</button>
        </div>
        <br />
        <textarea value={output} onInput={e => setOutput(e.currentTarget.value)} />
        <br />
        <button onClick={() => createDoot(output)} >Create Doot from above box</button>
        <button onClick={() => reply(itemId, output)} >Create reply (parent from Item Id field above)</button>
        <br />
        <br />
        <div>
          <label for="followUsername">
            Username:
            <input name="followUsername" type="text" onInput={e => setFollowUsername(e.currentTarget.value)} value={followUsername} />
          </label>
          <br />
          <button onClick={() => follow(followUsername, true)}>Follow</button>
          <button onClick={() => follow(followUsername, false)}>Unfollow</button>
          <button onClick={() => getProfile(followUsername)}>Get Profile</button>
          <br />
          <button onClick={() => getPosts(followUsername, followLimit)}>Get Posts</button>
          <button onClick={() => getFollowers(followUsername, followLimit)}>Get Followers</button>
          <button onClick={() => getFollowing(followUsername, followLimit)}>Get Following</button>
          <br />
          <label for="followLimit">
            Limit:
            <input name="followLimit" type="number" onInput={e => setFollowLimit(e.currentTarget.value)} value={followLimit} />
          </label>
        </div>
      </header>
    </div>
  );
}

export default App;

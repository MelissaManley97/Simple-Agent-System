
# Simple-Agent-System
Take-home interview for research assistant position

**Description:**

This repository contains the files necessary for the PingAgent and PongAgent example in the research assistant take-home interview. For the bonus, more interesting agents were written. To complete this requirement, agents to support client/server messaging between two hosts were created. These agents are titled ChatServerAgent and ChatClientAgent. Videos of both the Ping/Pong example and the additional Chat/Messaging agents are included in this repository as well.

**Version Support:**
This program runs on Ubuntu 16.04.4 LTS.

**Build Instructions:**

After cloning the repository, open a terminal and change directory to where the repository was saved.
Run the following command: "make"
After this runs, all the java files will be built.

**Run Instructions:**

*To run the Ping/Pong example:*

On host A:
run the following command: "java AgentSystem PongAgent"

Then, on host B:
run the following command: "java AgentSystem PingAgent"



*To run the Chat example:*

On host A:
run the following command: "java AgentSystem ChatServerAgent"

Then, on host B:
run the following command: "java AgentSystem ChatClientAgent"

Once the client is connected, they can make contact with the server and start a conversation.
Type "end chat" on either the ChatServerAgent or ChatClientAgent to end the chat.

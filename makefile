

JFLAGS = -g
JC = javac
JVM= java 

.SUFFIXES: .java .class


.java.class:
	 $(JC) $(JFLAGS) $*.java

CLASSES = \
        Agent.java \
        AgentSystem.java \
        PongAgent.java \
        PingAgent.java \
        ChatClientAgent.java \
	ChatServerAgent.java \
	SocketPacketPackage.java

default: classes

classes: $(CLASSES:.java=.class)

run: $(MAIN).class
	$(JVM) $(MAIN)

clean:
	$(RM) *.class


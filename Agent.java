
import java.util.UUID;

/**
 * Agent class that contains an agent UID and classname.
 */
public class Agent {

    private String UID; // Unique ID of the agent.
    private String className;  // Class name / type of the agent.

    /**
     * Constructor for Agent.
     *
     * @param name Classname of the agent.
     */
    public Agent(String name) {
        className = name;
        setUID();
    }

    /**
     * Sets the unique ID of this agent.
     */
    private void setUID() {
        UID = UUID.randomUUID().toString();
    }

    /**
     * Retrieves the unique ID of this agent.
     *
     * @return String representing this agent's UID.
     */
    public String getUID() {
        return UID;
    }

    /**
     * Retrieves the classname of this agent.
     *
     * @return String representing this agent's classname.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the classname of this agent.
     *
     * @param toSet String representing what "classname" will be set to.
     */
    public void setClassName(String toSet) {
        className = toSet;
    }

    /**
     * Each agent has a "start" method that is intended to be overridden.
     *
     * @return Boolean representing completion of the start method.
     */
    public boolean start() {
        return true;
    }
}

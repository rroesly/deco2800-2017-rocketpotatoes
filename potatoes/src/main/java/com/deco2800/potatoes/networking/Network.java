package com.deco2800.potatoes.networking;

import com.deco2800.potatoes.entities.AbstractEntity;
import com.deco2800.potatoes.entities.player.Player;
import com.deco2800.potatoes.entities.trees.AbstractTree;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class Network {

    private Network() {
        // Hide public constructor
    }

    /**
     * Registers our classes for serialization, to be used by both client and server in their initialization.
     * @param endPoint
     */
    public static void register(EndPoint endPoint) {
        Kryo k = endPoint.getKryo();

        // Kyro magic (with overhead) to automatically register and initialize (without requiring default constructors)
        k.setRegistrationRequired(false);
        ((Kryo.DefaultInstantiatorStrategy) k.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    // Define our custom types/containers for serialization here
    // (then register)

    // Client...Message is the format for a message to the host
    // Host...Message is the format for a message sent to clients
    // Anything else can be used for either

    /* Message sent when a connection is initially made,
     * should be the first message between a client and host */
    public static class ClientConnectionRegisterMessage {
        private String name;
        private Player player;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }
    }

    public static class HostDisconnectMessage {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class HostPlayerDisconnectedMessage {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    /* Message telling the client they are ready to play */
    public static class HostPlayReadyMessage {
    }

    /* Message telling other clients of a new player */
    public static class HostNewPlayerMessage {
        private String name;
        private int id;
        private Player player;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }
    }

    /* Message telling new clients of an existing player, doesn't create the player entity when processing this */
    public static class HostExistingPlayerMessage {
        private String name;
        private int id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }


    /* Message confirming connection, gives the client their id and the seed */
    public static class HostConnectionConfirmMessage {
        private int id;
        private long seed;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public long getSeed() {
            return seed;
        }

        public void setSeed(long seed) {
            this.seed = seed;
        }
    }

    /* Direct response to a HostEntityCreationMessage, this message is sent to all clients
     * to tell them of this entities existence and it's unique identifier.
     */
    public static class HostEntityCreationMessage {
        private AbstractEntity entity;
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public AbstractEntity getEntity() {
            return entity;
        }

        public void setEntity(AbstractEntity entity) {
            this.entity = entity;
        }
    }

    public static class HostEntityDestroyMessage {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    /* Message indicating our player moved */
    public static class ClientPlayerUpdatePositionMessage {
        private float x;

        private float y;

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }

    /* Message indicating our player wants to build something */
    public static class ClientBuildOrderMessage {
        private AbstractTree tree;

        public AbstractTree getTree() {
            return tree;
        }

        public void setTree(AbstractTree tree) {
            this.tree = tree;
        }
    }

    /* Message from the host indicating a new position of an entity */
    public static class HostEntityUpdatePositionMessage {
        private float x;

        private float y;
        
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }

    /* Message from the host indicating an entity's progress has changed (using the HasProgress interface) */
    public static class HostEntityUpdateProgressMessage {
        private int progress;
        private int id;

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    /* Simple chat message object */
    public static class ClientChatMessage {
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        private String message;
    }

    /* Chat message object sent with sender ID */
    public static class HostChatMessage {
        private String message;
        private int id;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

}

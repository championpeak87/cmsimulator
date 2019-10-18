package fiitstu.gulis.cmsimulator.elements;

import java.io.Serializable;

/**
 * Object for the node in the derivation tree simulation
 */
public class DerivationTreeStateNode implements Serializable {
    private char symbol;
    private DerivationTreeStateNode predecessor;
    private int positionX;
    private int positionY;
    private int level;
    private int levelLength;
    private int color;

    public DerivationTreeStateNode(char symbol, DerivationTreeStateNode predecessor, int positionX, int positionY, int level, int levelLength, int color) {
        this.symbol = symbol;
        this.predecessor = predecessor;
        this.positionX = positionX;
        this.positionY = positionY;
        this.level = level;
        this.levelLength = levelLength;
        this.color = color;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public DerivationTreeStateNode getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(DerivationTreeStateNode predecessor) {
        this.predecessor = predecessor;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int getPositionY) {
        this.positionY = getPositionY;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevelLength() {
        return levelLength;
    }

    public void setLevelLength(int levelLength) {
        this.levelLength = levelLength;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

}

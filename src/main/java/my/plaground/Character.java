package my.plaground;

import static java.lang.Math.*;

public abstract class Character {

    protected double initHp;
    protected double hp;
    protected int level;
    protected int resistance;
    protected double maxRange;
    protected Faction faction;
    protected Position position;
    protected RandomDataGeneratorInterface randomDataGenerator;

    public int getLevel() {
        return level;
    }

    protected void setLevel(int level) {
        if (level <= 0)
            throw new RuntimeException("Invalid power");

        this.level = level;
    }

    public double getHp() {
        return hp;
    }

    public double getInitHp() {
        return initHp;
    }


    public int getResistance() {
        return resistance;
    }

    public Position getPosition() {
        return position;
    }

    protected abstract double getSpecialDamage(Character enemy);

    public abstract int attackDamage();

    public int empoweredDamage() {
        return this.attackDamage() * this.level;
    }

    public boolean isDead() {
        return this.hp == 0;
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public void attack(Character enemy) {
        if (!canAttack(enemy))
            return;

        double totalDamage = this.calculateTotalDamage(enemy);
        enemy.defend(totalDamage);
    }

    private boolean canAttack(Character enemy) {
        double enemyDistance = this.position.distanceFrom(enemy.getPosition());
        return this.isAlive()
                && enemyDistance <= maxRange
                && !enemy.isAlly(this)
                && !enemy.isMe(this);
    }

    public double calculateTotalDamage(Character enemy) {
        double totalDamage = (this.empoweredDamage() * this.getSpecialDamage(enemy)) / enemy.getResistance();
        if(enemy.level - this.level >= 5 )
            totalDamage *= 0.5;
        else if(this.level - enemy.level >= 5)
            totalDamage += totalDamage *  0.5;

        return totalDamage;
    }

    public void defend(double damage) {
        if (damage <= 0)
            return;

        this.hp -= this.calculateReceivedDamage(damage);
        if (this.hp <= 0)
            this.hp = 0;
    }

    protected double calculateReceivedDamage(double damage) {
        return damage;
    }

    public void heal(Character target) {
        if (!canHeal(target))
            return;

        double hps = this.calculateHealingHps();
        target.getHealed(hps);
    }

    private boolean canHeal(Character target) {
        return      (this.isAlive() && target.isAlive())
                &&  (this.isMe(target) || target.isAlly(this));
    }

    protected abstract double calculateHealingHps();

    protected void getHealed(double hps) {
        this.hp = min(this.hp + hps, this.initHp);
    }


    public void setPosition(Position position) {
        this.position = position;
    }

    public void joinFaction(Faction faction){
        if(this.faction != null)
            return;

        this.faction = faction;
    }

    public void leaveFaction(){
        this.faction = null;
    }

    public boolean isAlly(Character character){
        return this.faction != null && this.faction.equals(character.faction);
    }


    private boolean isMe(Character character){
      return this.equals(character);
    }
}

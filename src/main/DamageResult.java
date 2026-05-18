package main;

public class DamageResult {
    public final double damage;
    public final boolean isCrit;

    public DamageResult(double damage, boolean isCrit) {
        this.damage = damage;
        this.isCrit = isCrit;
    }
}
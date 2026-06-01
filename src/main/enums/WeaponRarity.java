package main.enums;

public enum WeaponRarity {
    // Defined values based on your doc: (multiplier, flatBonus, speedReduc, sizeInc, critChance, critDmg)
    BRONZE(1.0, 1, 0.05, 0.10, 0.05, 0.25),
    SILVER(1.2, 1, 0.06, 0.12, 0.06, 0.30),
    GOLD(1.4, 1, 0.07, 0.16, 0.08, 0.40),
    DIAMOND(2.0, 2, 0.10, 0.20, 0.10, 0.50);

    public final double multiplier;    // For Damage/Execution
    public final int flatBonus;        // For Projectiles/Bounces
    public final double speedReduction;// For Attack Speed (-5% etc)
    public final double sizeIncrease;  // For Attack Size (+10% etc)
    public final double critChanceAdd; // For Crit Rate (+5% etc)
    public final double critDamageAdd; // For Crit Damage (+25% etc)

    WeaponRarity(double mult, int flat, double speed, double size, double critC, double critD) {
        this.multiplier = mult;
        this.flatBonus = flat;
        this.speedReduction = speed;
        this.sizeIncrease = size;
        this.critChanceAdd = critC;
        this.critDamageAdd = critD;
    }
}
package main.enums;

public class DifficultyConfig {
	public final double creditGainMult; // credits per tick multiplier
	public final double rampMult; // how fast credit gain accelerates
	public final double enemyStatMult; // scales enemy HP/ATK/speed
	public final double bossStatMult; // extra multiplier for roaming bosses
	public final int spendPct; // % of budget spent per spawn attempt

	public DifficultyConfig(double cgm, double rm, double esm, double bsm, int sp) {
		creditGainMult = cgm;
		rampMult = rm;
		enemyStatMult = esm;
		bossStatMult = bsm;
		spendPct = sp;
	}
}

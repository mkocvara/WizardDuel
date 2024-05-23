package com.mk.wizardduel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Player
{
	PLAYER_ONE(0),
	PLAYER_TWO(1);

	public static final int COUNT = 2;
	public final int intValue;

	Player(int value)
	{
		intValue = value;
	}

	private static final List<Player> mValues = new ArrayList<>();
	static { mValues.addAll(Arrays.asList(Player.values())); }

	public static int getInt(Player player)
	{
		for (Player p : Player.values())
			if (p.equals(player))
				return mValues.indexOf(p);

		return -1;
	}

	public static Player getPlayer(int playerNumber)
	{
		return mValues.get(playerNumber);
	}
}

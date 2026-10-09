import asyncio
import genshin

UID = 851880405
COOKIES = {
    "ltuid_v2": "188852545",
    "ltmid_v2": "199n8h80m2_hy",
    "ltoken_v2": "v2_CAISDGM5b3FhcTNzM2d1OBokZDk2OTUwYmItYjhhNi00ZGU3LTkxMTAtM2M4MjI2Y2U4MGI1IOOLo9YGKKiN49QBMMHShlpCC2Jic19vdmVyc2VhagJTRw.48XIagAAAAAB.MEQCIGCxxilahMVIZTAOByHFSuavbaljV9BcteeOrUWX4lutAiA99WYuMoUKZ5OjNdghCbQkhBjNlKrtjY0QRAQJ_YKy5g",
    "account_id_v2": "188852545",
    "account_mid_v2": "199n8h80m2_hy",
}

MAIN_STAT_MAP = {
    2: "hp",
    3: "hp%",
    5: "atk",
    6: "atk%",
    8: "def",
    9: "def%",
    20: "cr",
    22: "cd",
    23: "er",
    26: "heal",
    28: "em",
    30: "phys",
    40: "pyro",
    41: "electro",
    42: "hydro",
    43: "dendro",
    44: "anemo",
    45: "geo",
    46: "cryo",
}

SUB_STAT_MAP = {
    2: "hp",
    3: "hp%",
    5: "atk",
    6: "atk%",
    8: "def",
    9: "def%",
    20: "cr",
    22: "cd",
    23: "er",
    28: "em",
}

async def generate():
    client = genshin.Client(COOKIES, game=genshin.Game.GENSHIN, region=genshin.Region.OVERSEAS)
    print("Fetching detailed characters from HoYoLAB...")
    det = await client.get_genshin_detailed_characters(UID)
    print(f"Retrieved {len(det.characters)} characters.")

    commands = []

    # World and Player Progression
    commands.append("prop player ar 60")
    commands.append("prop player wl 8")
    commands.append("quest skip_to_7")
    commands.append("achievement grantall")

    # Give standard materials & mora
    commands.append("give 201 x100000000") # 100M Mora
    commands.append("give 202 x100000")    # 100k Primogems
    commands.append("give 223 x1000")      # 1000 Intertwined Fate
    commands.append("give 224 x1000")      # 1000 Acquaint Fate

    for c in det.characters:
        # Determine highest talent level
        skill_lv = 10
        if hasattr(c, "skills") and c.skills:
            active_skills = [s.level for s in c.skills if getattr(s, "skill_type", 0) == 1]
            if active_skills:
                skill_lv = max(active_skills)

        # 1. Avatar
        commands.append(f"give {c.id} lv{c.level} c{c.constellation} sl{skill_lv}")

        # 2. Weapon
        if c.weapon:
            commands.append(f"give {c.weapon.id} lv{c.weapon.level} r{c.weapon.refinement}")

        # 3. Artifacts
        for a in c.artifacts:
            main_short = MAIN_STAT_MAP.get(a.main_stat.info.type)
            if not main_short:
                # Fallback to id
                main_short = str(a.main_stat.info.type)

            subs = []
            for s in a.sub_stats:
                s_short = SUB_STAT_MAP.get(s.info.type)
                if not s_short:
                    continue
                rolls = getattr(s, "times", 0) + 1
                subs.append(f"{s_short},{rolls}")

            sub_str = " ".join(subs)
            if sub_str:
                commands.append(f"give {a.id} lv{a.level} {main_short} {sub_str}")
            else:
                commands.append(f"give {a.id} lv{a.level} {main_short}")

    out_file = "/home/venomsnake/Desktop/genshi/AstaPS/import_venomsnakee.txt"
    with open(out_file, "w", encoding="utf-8") as f:
        f.write("\n".join(commands) + "\n")

    print(f"Generated {len(commands)} commands in {out_file}")

if __name__ == "__main__":
    asyncio.run(generate())

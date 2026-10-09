import asyncio
import json
import genshin

UID = 654257927
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

async def run():
    client = genshin.Client(COOKIES, game=genshin.Game.GENSHIN, region=genshin.Region.OVERSEAS)
    print(f"Fetching detailed data for UID {UID} from HoYoLAB...")
    det = await client.get_genshin_detailed_characters(UID)
    print(f"Successfully retrieved {len(det.characters)} characters.")

    full_data = []
    commands = []

    for c in det.characters:
        char_dict = {
            "id": c.id,
            "name": c.name,
            "element": getattr(c, "element", None),
            "rarity": c.rarity,
            "level": c.level,
            "constellation": c.constellation,
            "fetter": getattr(c, "fetter", 10),
            "weapon": None,
            "artifacts": []
        }

        # 1. Avatar command
        skill_lv = 10
        if hasattr(c, "skills") and c.skills:
            active_skills = [s.level for s in c.skills if getattr(s, "skill_type", 0) == 1]
            if active_skills:
                skill_lv = max(active_skills)
        commands.append(f"give {c.id} lv{c.level} c{c.constellation} sl{skill_lv}")

        # 2. Weapon
        if c.weapon:
            w = c.weapon
            char_dict["weapon"] = {
                "id": w.id,
                "name": w.name,
                "rarity": w.rarity,
                "level": w.level,
                "refinement": w.refinement,
                "promote_level": getattr(w, "promote_level", 6)
            }
            commands.append(f"give {w.id} lv{w.level} r{w.refinement}")

        # 3. Artifacts
        for a in c.artifacts:
            art_dict = {
                "id": a.id,
                "name": a.name,
                "pos": getattr(a, "pos", None),
                "pos_name": getattr(a, "pos_name", None),
                "rarity": a.rarity,
                "level": a.level,
                "set": {
                    "id": getattr(a.set, "id", None),
                    "name": getattr(a.set, "name", None)
                },
                "main_stat": {
                    "type": a.main_stat.info.type,
                    "name": a.main_stat.info.name,
                    "value": a.main_stat.value
                },
                "sub_stats": []
            }

            main_short = MAIN_STAT_MAP.get(a.main_stat.info.type, str(a.main_stat.info.type))
            subs = []
            for s in a.sub_stats:
                rolls = getattr(s, "times", 0) + 1
                art_dict["sub_stats"].append({
                    "type": s.info.type,
                    "name": s.info.name,
                    "value": s.value,
                    "rolls": rolls
                })
                s_short = SUB_STAT_MAP.get(s.info.type)
                if s_short:
                    subs.append(f"{s_short},{rolls}")

            char_dict["artifacts"].append(art_dict)

            sub_str = " ".join(subs)
            if sub_str:
                commands.append(f"give {a.id} lv{a.level} {main_short} {sub_str}")
            else:
                commands.append(f"give {a.id} lv{a.level} {main_short}")

        full_data.append(char_dict)

    # Save JSON dump
    json_path = "/home/venomsnake/Desktop/genshi/scraped_654257927.json"
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(full_data, f, indent=2, ensure_ascii=False)
    print(f"Saved complete JSON data ({len(full_data)} characters, {sum(len(c['artifacts']) for c in full_data)} artifacts) to {json_path}")

    # Save Command TXT
    txt_path = "/home/venomsnake/Desktop/genshi/commands_654257927.txt"
    with open(txt_path, "w", encoding="utf-8") as f:
        f.write("\n".join(commands) + "\n")
    print(f"Saved {len(commands)} give commands to {txt_path}")

if __name__ == "__main__":
    asyncio.run(run())

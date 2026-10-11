#!/usr/bin/env python3
import json
import pymongo

UID = 851880405

def main():
    client = pymongo.MongoClient("mongodb://localhost:27017")
    db = client["grasscutter"]

    with open("/home/venomsnake/Desktop/genshi/AstaPS/resources/ExcelBinOutput/AvatarExcelConfigData.json") as f:
        avatars_config = {a["id"]: a for a in json.load(f)}

    with open("/home/venomsnake/Desktop/genshi/AstaPS/resources/ExcelBinOutput/AvatarSkillDepotExcelConfigData.json") as f:
        depots_config = {d["id"]: d for d in json.load(f)}

    user_avatars = list(db["avatars"].find({"ownerId": UID}))
    print(f"Total avatars found for UID {UID}: {len(user_avatars)}")

    four_star_updated = 0
    four_star_already = 0
    five_star_updated = 0
    five_star_already = 0
    skipped_list = []

    for a in user_avatars:
        aid = a["avatarId"]
        cfg = avatars_config.get(aid, {})
        q = cfg.get("qualityType")

        # Skip travelers
        if aid in (10000005, 10000007):
            skipped_list.append((aid, cfg.get("iconName"), "Traveler"))
            continue

        depot_id = a.get("skillDepotId") or cfg.get("skillDepotId")
        depot = depots_config.get(depot_id)
        if not depot:
            skipped_list.append((aid, cfg.get("iconName"), "No skill depot"))
            continue

        valid_talents = [t for t in depot.get("talents", []) if t > 0]
        cur_talents = a.get("talentIdList", [])

        if q == "QUALITY_PURPLE":
            # 4-star -> C6
            if len(valid_talents) == 6:
                target_talents = valid_talents[:6]
                if cur_talents == target_talents:
                    four_star_already += 1
                else:
                    db["avatars"].update_one({"_id": a["_id"]}, {"$set": {"talentIdList": target_talents}})
                    four_star_updated += 1
            else:
                skipped_list.append((aid, cfg.get("iconName"), f"4-star with {len(valid_talents)} talents"))

        elif q in ("QUALITY_ORANGE", "QUALITY_ORANGE_SP"):
            # 5-star -> C3
            if len(valid_talents) >= 3:
                target_talents = valid_talents[:3]
                if cur_talents == target_talents:
                    five_star_already += 1
                else:
                    db["avatars"].update_one({"_id": a["_id"]}, {"$set": {"talentIdList": target_talents}})
                    five_star_updated += 1
            else:
                skipped_list.append((aid, cfg.get("iconName"), f"5-star with {len(valid_talents)} talents"))

    print("\n--- Summary ---")
    print(f"4-Stars: {four_star_already} already C6, {four_star_updated} updated to C6")
    print(f"5-Stars: {five_star_already} already C3, {five_star_updated} updated to C3")
    print(f"Skipped avatars ({len(skipped_list)}):")
    for s in skipped_list:
        print(f"  ID {s[0]} ({s[1]}): {s[2]}")

if __name__ == "__main__":
    main()

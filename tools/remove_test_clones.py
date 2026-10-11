#!/usr/bin/env python3
import pymongo

UID = 851880405
CLONE_IDS = [10000901, 10000902, 10000903, 10000904]
REAL_IDS = [10000106, 10000046, 10000116, 10000125]

def main():
    client = pymongo.MongoClient("mongodb://localhost:27017")
    db = client["grasscutter"]

    # 1. Remove from db.avatars
    res_av = db["avatars"].delete_many({"ownerId": UID, "avatarId": {"$in": CLONE_IDS}})
    print(f"Deleted {res_av.deleted_count} clone avatars from db.avatars.")

    # 2. Unequip items attached to clone avatars
    res_it = db["items"].update_many(
        {"ownerId": UID, "equipCharacter": {"$in": CLONE_IDS}},
        {"$set": {"equipCharacter": 0}}
    )
    print(f"Unequipped {res_it.modified_count} item(s) from clone avatars.")

    # 3. Clean up teams in db.players
    player = db["players"].find_one({"_id": UID})
    if player and "teamManager" in player:
        tm = player["teamManager"]
        teams = tm.get("teams", {})
        modified_teams = False
        for tid, tinfo in teams.items():
            avs = tinfo.get("avatars", [])
            new_avs = [aid for aid in avs if aid not in CLONE_IDS]
            if new_avs != avs:
                tinfo["avatars"] = new_avs
                modified_teams = True
                print(f"Removed clone from Team {tid}: {avs} -> {new_avs}")

        if modified_teams:
            db["players"].update_one(
                {"_id": UID},
                {"$set": {"teamManager.teams": teams}}
            )
            print("Successfully updated player teams in db.players.")

    # 4. Verify real avatars are untouched
    print("\nVerifying real counterparts:")
    for rid in REAL_IDS:
        a = db["avatars"].find_one({"ownerId": UID, "avatarId": rid})
        w = db["items"].find_one({"ownerId": UID, "equipCharacter": rid, "itemId": {"$gte": 10000, "$lte": 20000}})
        relics_cnt = db["items"].count_documents({"ownerId": UID, "equipCharacter": rid, "itemId": {"$gt": 20000}})
        lvl = a.get("level") if a else None
        wid = w.get("itemId") if w else None
        print(f"  Real Avatar {rid}: Lvl {lvl}, Weapon {wid}, Relics: {relics_cnt}")

    total_avs = db["avatars"].count_documents({"ownerId": UID})
    print(f"\nTotal avatars owned after cleanup: {total_avs}")

if __name__ == "__main__":
    main()

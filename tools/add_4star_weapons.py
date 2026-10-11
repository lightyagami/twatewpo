#!/usr/bin/env python3
import json
import pymongo
from bson import ObjectId

UID = 851880405
EXP_90 = 6042650
ILLEGAL = {11411, 14411}
TEST_IDS = [ObjectId("6acb87520432272ec8ddb1cc"), ObjectId("6acb87520432272ec8ddb1cd")]

def main():
    client = pymongo.MongoClient("mongodb://localhost:27017")
    db = client["grasscutter"]

    # 1. Clean up test documents
    del_res = db["items"].delete_many({"_id": {"$in": TEST_IDS}})
    if del_res.deleted_count > 0:
        print(f"Cleaned up {del_res.deleted_count} test item(s).")

    # 2. Load weapon configurations
    with open("/home/venomsnake/Desktop/genshi/AstaPS/resources/ExcelBinOutput/WeaponExcelConfigData.json") as f:
        weapons_data = json.load(f)

    four_stars = {}
    for w in weapons_data:
        wid = w["id"]
        if w.get("rankLevel") == 4 and 11100 <= wid <= 16000 and wid not in ILLEGAL:
            four_stars[wid] = w

    print(f"Total valid 4-star weapons recognized: {len(four_stars)}")

    # 3. Upgrade all existing 4-star weapons owned by UID to Level 90 R5
    existing_items = list(db["items"].find({"ownerId": UID, "itemId": {"$in": list(four_stars.keys())}}))
    print(f"Found {len(existing_items)} existing 4-star weapons in account.")

    updated_existing = 0
    for item in existing_items:
        needs_update = False
        updates = {}
        if item.get("level") != 90 or item.get("promoteLevel") != 6 or item.get("totalExp") != EXP_90 or item.get("exp") != 0:
            updates["level"] = 90
            updates["promoteLevel"] = 6
            updates["totalExp"] = EXP_90
            updates["exp"] = 0
            needs_update = True
        if item.get("refinement") != 4:
            updates["refinement"] = 4
            needs_update = True

        if needs_update:
            db["items"].update_one({"_id": item["_id"]}, {"$set": updates})
            updated_existing += 1

    print(f"Upgraded {updated_existing} existing 4-star weapons to Level 90 R5.")

    # 4. Create 2 new copies for each 4-star weapon
    new_docs = []
    for wid, wcfg in four_stars.items():
        affixes = [s for s in wcfg.get("skillAffix", []) if s > 0]
        for _ in range(2):
            doc = {
                "_id": ObjectId(),
                "ownerId": UID,
                "itemId": wid,
                "count": 1,
                "level": 90,
                "exp": 0,
                "totalExp": EXP_90,
                "promoteLevel": 6,
                "favourite": False,
                "locked": False,
                "affixes": affixes,
                "refinement": 4, # R5
                "mainPropId": 0,
                "relicStarred": False,
                "equipCharacter": 0
            }
            new_docs.append(doc)

    print(f"Inserting {len(new_docs)} new 4-star weapon copies (2 per weapon)...")
    res = db["items"].insert_many(new_docs)
    print(f"Successfully inserted {len(res.inserted_ids)} new weapons into database.")

    # 5. Audit & Verification
    all_4s_after = list(db["items"].find({"ownerId": UID, "itemId": {"$in": list(four_stars.keys())}}))
    print(f"\n=== Verification Report ===")
    print(f"Total 4-star weapons now owned: {len(all_4s_after)}")

    invalid_lvl = [i for i in all_4s_after if i.get("level") != 90 or i.get("promoteLevel") != 6]
    invalid_ref = [i for i in all_4s_after if i.get("refinement") != 4]
    print(f"Weapons not at Level 90 / Asc 6: {len(invalid_lvl)}")
    print(f"Weapons not at Refinement 5: {len(invalid_ref)}")

    unequipped_counts = {}
    for i in all_4s_after:
        wid = i["itemId"]
        if i.get("equipCharacter", 0) == 0:
            unequipped_counts[wid] = unequipped_counts.get(wid, 0) + 1

    missing_2_copies = [wid for wid in four_stars.keys() if unequipped_counts.get(wid, 0) < 2]
    print(f"Weapons with fewer than 2 unequipped copies: {len(missing_2_copies)}")

    total_weapons_total = db["items"].count_documents({"ownerId": UID, "itemId": {"$gte": 10000, "$lte": 20000}})
    print(f"Total weapons owned in account: {total_weapons_total} / 2000 limit")

if __name__ == "__main__":
    main()

#!/usr/bin/env python3
import json
import pymongo

UID = 851880405

def main():
    client = pymongo.MongoClient("mongodb://localhost:27017")
    db = client["grasscutter"]

    with open("/home/venomsnake/Desktop/genshi/AstaPS/resources/ExcelBinOutput/AvatarSkillDepotExcelConfigData.json") as f:
        depots = json.load(f)
    depot_map = {d["id"]: d for d in depots}

    with open("/home/venomsnake/Desktop/genshi/AstaPS/resources/ExcelBinOutput/AvatarExcelConfigData.json") as f:
        avatars = json.load(f)
    four_stars = {a["id"]: a for a in avatars if a.get("qualityType") == "QUALITY_PURPLE" and a["id"] > 10000000 and a["id"] < 11000000}

    user_avatars = list(db["avatars"].find({"ownerId": UID, "avatarId": {"$in": list(four_stars.keys())}}))
    print(f"Found {len(user_avatars)} 4-star avatars for player {UID}.")

    updated = 0
    for a in user_avatars:
        depot_id = a.get("skillDepotId")
        depot = depot_map.get(depot_id)
        if not depot:
            continue
        talents = [t for t in depot.get("talents", []) if t > 0]
        if len(talents) == 6:
            db["avatars"].update_one(
                {"_id": a["_id"]},
                {"$set": {"talentIdList": talents}}
            )
            updated += 1

    print(f"Successfully set {updated} 4-star avatars to C6!")

if __name__ == "__main__":
    main()

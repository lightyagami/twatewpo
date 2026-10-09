#!/usr/bin/env python3
import json
import sys
import pymongo
from bson import ObjectId

UID = 851880405
SCRAPED_FILE = "/home/venomsnake/Desktop/genshi/scraped_851880405.json"
RELIQUARY_FILE = "/home/venomsnake/Desktop/genshi/AstaPS/resources/ExcelBinOutput/ReliquaryExcelConfigData.json"
MAIN_PROP_FILE = "/home/venomsnake/Desktop/genshi/AstaPS/resources/ExcelBinOutput/ReliquaryMainPropExcelConfigData.json"
AFFIX_FILE = "/home/venomsnake/Desktop/genshi/AstaPS/resources/ExcelBinOutput/ReliquaryAffixExcelConfigData.json"
WEAPON_FILE = "/home/venomsnake/Desktop/genshi/AstaPS/resources/ExcelBinOutput/WeaponExcelConfigData.json"

PROP_MAP = {
    2: 'FIGHT_PROP_HP',
    3: 'FIGHT_PROP_HP_PERCENT',
    5: 'FIGHT_PROP_ATTACK',
    6: 'FIGHT_PROP_ATTACK_PERCENT',
    8: 'FIGHT_PROP_DEFENSE',
    9: 'FIGHT_PROP_DEFENSE_PERCENT',
    20: 'FIGHT_PROP_CRITICAL',
    22: 'FIGHT_PROP_CRITICAL_HURT',
    23: 'FIGHT_PROP_CHARGE_EFFICIENCY',
    26: 'FIGHT_PROP_HEAL_ADD',
    28: 'FIGHT_PROP_ELEMENT_MASTERY',
    30: 'FIGHT_PROP_PHYSICAL_ADD_HURT',
    40: 'FIGHT_PROP_FIRE_ADD_HURT',
    41: 'FIGHT_PROP_ELEC_ADD_HURT',
    42: 'FIGHT_PROP_WATER_ADD_HURT',
    43: 'FIGHT_PROP_GRASS_ADD_HURT',
    44: 'FIGHT_PROP_WIND_ADD_HURT',
    45: 'FIGHT_PROP_ROCK_ADD_HURT',
    46: 'FIGHT_PROP_ICE_ADD_HURT',
}

def get_min_promote_level(level):
    if level > 80: return 6
    elif level > 70: return 5
    elif level > 60: return 4
    elif level > 50: return 3
    elif level > 40: return 2
    elif level > 20: return 1
    return 0

def main():
    print(f"Connecting to MongoDB...")
    client = pymongo.MongoClient("mongodb://localhost:27017")
    db = client["grasscutter"]

    player = db["players"].find_one({"_id": UID})
    if not player:
        print(f"ERROR: Player {UID} not found in database!")
        sys.exit(1)

    print(f"Loading scraped gear data from {SCRAPED_FILE}...")
    with open(SCRAPED_FILE, "r", encoding="utf-8") as f:
        chars = json.load(f)

    with open(RELIQUARY_FILE, "r", encoding="utf-8") as f:
        relics_list = json.load(f)
    relics_map = {r['id']: r for r in relics_list}

    with open(MAIN_PROP_FILE, "r", encoding="utf-8") as f:
        main_props_list = json.load(f)
    main_depots = {}
    for m in main_props_list:
        depot = m['propDepotId']
        if depot not in main_depots: main_depots[depot] = {}
        if m.get('weight', 0) > 0:
            main_depots[depot][m['propType']] = m['id']

    with open(AFFIX_FILE, "r", encoding="utf-8") as f:
        affix_list = json.load(f)
    affix_depots = {}
    for a in affix_list:
        depot = a['depotId']
        if depot not in affix_depots: affix_depots[depot] = {}
        pt = a['propType']
        if pt not in affix_depots[depot]: affix_depots[depot][pt] = []
        affix_depots[depot][pt].append((a.get('propValue', 0), a['id']))

    for d in affix_depots:
        for pt in affix_depots[d]:
            affix_depots[d][pt].sort(key=lambda x: x[0])

    with open(WEAPON_FILE, "r", encoding="utf-8") as f:
        weapon_list = json.load(f)
    weapon_map = {w['id']: w for w in weapon_list}

    char_ids = [c['id'] for c in chars]
    print(f"Unequipping existing items on {len(char_ids)} avatars for player {UID}...")
    res = db["items"].update_many(
        {"ownerId": UID, "equipCharacter": {"$in": char_ids}},
        {"$set": {"equipCharacter": 0}}
    )
    print(f"Unequipped {res.modified_count} existing items (safely moved to bag).")

    weapon_docs = []
    artifact_docs = []

    for c in chars:
        cid = c['id']
        w = c.get('weapon')
        if w:
            promote = get_min_promote_level(w['level'])
            ref = max(0, w['refinement'] - 1)
            w_excel = weapon_map[w['id']]
            skill_affixes = [s for s in w_excel.get('skillAffix', []) if s > 0]
            wdoc = {
                '_id': ObjectId(),
                'ownerId': UID,
                'itemId': w['id'],
                'count': 1,
                'level': w['level'],
                'exp': 0,
                'totalExp': 0,
                'promoteLevel': promote,
                'favourite': False,
                'locked': False,
                'affixes': skill_affixes,
                'refinement': ref,
                'equipCharacter': cid,
            }
            weapon_docs.append(wdoc)

        for a in c['artifacts']:
            rdata = relics_map[a['id']]
            main_type = PROP_MAP[a['main_stat']['type']]
            depot = rdata['mainPropDepotId']
            main_id = main_depots[depot][main_type]

            affix_ids = []
            adepot = rdata['appendPropDepotId']
            for s in a['sub_stats']:
                stype = PROP_MAP[s['type']]
                cand = affix_depots[adepot][stype]
                tier_id = cand[-1][1]
                for _ in range(s['rolls']):
                    affix_ids.append(tier_id)

            adoc = {
                '_id': ObjectId(),
                'ownerId': UID,
                'itemId': a['id'],
                'count': 1,
                'level': a['level'] + 1,
                'exp': 0,
                'totalExp': 0,
                'promoteLevel': 0,
                'favourite': False,
                'locked': False,
                'mainPropId': main_id,
                'appendPropIdList': affix_ids,
                'relicStarred': False,
                'purchasedAppendPropIdList': [],
                'definiteAppendPropIdList': [],
                'equipCharacter': cid,
            }
            artifact_docs.append(adoc)

    all_docs = weapon_docs + artifact_docs
    print(f"Inserting {len(weapon_docs)} weapons and {len(artifact_docs)} artifacts ({len(all_docs)} total) into database...")
    insert_res = db["items"].insert_many(all_docs)
    print(f"Successfully inserted {len(insert_res.inserted_ids)} items into 'items' collection!")

    new_weapons = db["items"].count_documents({"ownerId": UID, "equipCharacter": {"$in": char_ids}, "refinement": {"$exists": True}})
    new_artifacts = db["items"].count_documents({"ownerId": UID, "equipCharacter": {"$in": char_ids}, "mainPropId": {"$gt": 0}})
    print(f"Verification: {new_weapons} equipped weapons and {new_artifacts} equipped artifacts present for target avatars.")

if __name__ == "__main__":
    main()

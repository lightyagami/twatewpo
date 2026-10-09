import json
from pymongo import MongoClient

def main():
    client = MongoClient('mongodb://localhost:27017')
    db = client['grasscutter']

    with open('resources/ExcelBinOutput/AvatarExcelConfigData.json') as f:
        avatars_config = {a['id']: a for a in json.load(f)}
    with open('resources/ExcelBinOutput/AvatarSkillDepotExcelConfigData.json') as f:
        depots_config = {d['id']: d for d in json.load(f)}

    uid = 851880405

    # 1. Avatars
    avatars = list(db['avatars'].find({'ownerId': uid}))
    updated_avs = 0
    for av in avatars:
        av_id = av.get('avatarId')
        cfg = avatars_config.get(av_id)
        if not cfg:
            continue
        depot_id = av.get('skillDepotId') or cfg.get('skillDepotId')
        depot = depots_config.get(depot_id)

        needs_update = False
        new_fields = {}

        # If avatar is level 100 (or >90), set to level 90
        if av.get('level', 0) > 90:
            new_fields['level'] = 90
            new_fields['promoteLevel'] = 6
            new_fields['exp'] = 0
            needs_update = True

        # Constellations: if > 2 or was level 100, set to C2
        talents = av.get('talentIdList', [])
        if len(talents) > 2 or av.get('level', 0) == 100:
            if depot and depot.get('talents'):
                c2_talents = [t for t in depot['talents'][:2] if t > 0]
                if talents != c2_talents:
                    new_fields['talentIdList'] = c2_talents
                    needs_update = True

        # Max talents (level 10)
        if depot:
            skill_map = dict(av.get('skillLevelMap', {}))
            changed_skills = False
            all_skills = [s for s in depot.get('skills', []) if s > 0]
            if depot.get('energySkill'):
                all_skills.append(depot.get('energySkill'))
            for s in all_skills:
                if skill_map.get(str(s)) != 10:
                    skill_map[str(s)] = 10
                    changed_skills = True
            if changed_skills:
                new_fields['skillLevelMap'] = skill_map
                needs_update = True

        if needs_update:
            db['avatars'].update_one({'_id': av['_id']}, {'$set': new_fields})
            updated_avs += 1

    print(f'Successfully updated {updated_avs} avatars for UID {uid}.')

    # 2. Weapons
    weapons = list(db['items'].find({'ownerId': uid, 'itemId': {'$gte': 10000, '$lte': 20000}}))
    updated_weaps = 0
    for w in weapons:
        needs_update = False
        new_fields = {}
        if w.get('level', 0) > 90:
            new_fields['level'] = 90
            new_fields['promoteLevel'] = 6
            new_fields['exp'] = 0
            needs_update = True
        if w.get('refinement', 0) > 0:
            new_fields['refinement'] = 0  # R1 (refinement 0 is R1 in Grasscutter)
            needs_update = True

        if needs_update:
            db['items'].update_one({'_id': w['_id']}, {'$set': new_fields})
            updated_weaps += 1

    print(f'Successfully updated {updated_weaps} weapons for UID {uid}.')

if __name__ == '__main__':
    main()

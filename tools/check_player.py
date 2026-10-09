import time
from pymongo import MongoClient

def run_import():
    client = MongoClient('mongodb://localhost:27017')
    db = client['grasscutter']

    # Find account venomsnakee
    acc = db['accounts'].find_one({'username': 'venomsnakee'})
    if not acc:
        print("Account 'venomsnakee' not found!")
        return

    account_id = acc['_id']
    player = db['players'].find_one({'accountId': account_id})
    if not player:
        print("Player for account 'venomsnakee' has not logged in yet.")
        print("Please log into the game once with username 'venomsnakee', choose your twin and nickname.")
        return

    uid = player['_id']
    print(f"Found player UID {uid} for venomsnakee!")
    return uid

if __name__ == "__main__":
    run_import()

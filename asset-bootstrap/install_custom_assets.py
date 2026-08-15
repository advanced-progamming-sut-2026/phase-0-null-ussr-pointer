from pathlib import Path
import json
import shutil
import sys

ROOT = Path(__file__).resolve().parents[1]
PVZ_ASSETS = ROOT / "pvz-assets"
CUSTOM = ROOT / "custom-assets"

RESOURCES = PVZ_ASSETS / "RESOURCES.json"
PATCH = CUSTOM / "settings_resources_patch.json"
SOURCE_ATLAS = CUSTOM / "ATLASES" / "UI_SettingsMenu_768_00.PNG"
DEST_ATLAS = PVZ_ASSETS / "ATLASES" / "UI_SettingsMenu_768_00.PNG"
BACKUP = PVZ_ASSETS / "RESOURCES.before_custom_assets.json"

def fail(message):
    print(f"[ERROR] {message}")
    sys.exit(1)

if not PVZ_ASSETS.exists():
    fail(f"Missing folder: {PVZ_ASSETS}")

if not RESOURCES.exists():
    fail(f"Missing file: {RESOURCES}")

if not PATCH.exists():
    fail(f"Missing patch: {PATCH}")

if not SOURCE_ATLAS.exists():
    fail(f"Missing atlas: {SOURCE_ATLAS}")

print("[1/5] Loading RESOURCES.json...")
with RESOURCES.open("r", encoding="utf-8") as f:
    resources = json.load(f)

with PATCH.open("r", encoding="utf-8") as f:
    patch = json.load(f)

target_group_id = patch["target_group"]
target = next(
    (g for g in resources.get("groups", []) if g.get("id") == target_group_id),
    None
)
if target is None:
    fail(f"Could not find resource group: {target_group_id}")

print("[2/5] Creating backup...")
if not BACKUP.exists():
    shutil.copy2(RESOURCES, BACKUP)
    print(f"      Backup created: {BACKUP.name}")
else:
    print(f"      Backup already exists: {BACKUP.name}")

ids_to_replace = set(patch.get("resource_ids_to_replace", []))
prefixes = tuple(patch.get("resource_id_prefixes_to_replace", []))

print("[3/5] Removing previous custom settings resources...")
kept = []
for resource in target.get("resources", []):
    rid = resource.get("id", "")
    if rid in ids_to_replace:
        continue
    if prefixes and rid.startswith(prefixes):
        continue
    kept.append(resource)
target["resources"] = kept

# Find currently used slots AFTER removing our old custom entries.
used_slots = []
for group in resources.get("groups", []):
    for resource in group.get("resources", []) if isinstance(group.get("resources"), list) else []:
        slot = resource.get("slot")
        if isinstance(slot, int):
            used_slots.append(slot)

next_slot = max(used_slots, default=-1) + 1

print("[4/5] Installing custom resource entries...")
for template in patch["resources"]:
    resource = dict(template)
    resource["slot"] = next_slot
    next_slot += 1
    target["resources"].append(resource)

resources["slot_count"] = max(next_slot, max(used_slots, default=-1) + 1)

# Write atomically.
tmp = RESOURCES.with_suffix(".json.tmp")
with tmp.open("w", encoding="utf-8") as f:
    json.dump(resources, f, indent=2, ensure_ascii=False)
    f.write("\n")
tmp.replace(RESOURCES)

print("[5/5] Installing atlas...")
DEST_ATLAS.parent.mkdir(parents=True, exist_ok=True)
shutil.copy2(SOURCE_ATLAS, DEST_ATLAS)

print()
print("Custom settings assets installed successfully.")
print(f"Atlas: {DEST_ATLAS}")
print(f"Patched: {RESOURCES}")
print(f"slot_count: {resources['slot_count']}")
print()
print("You can run this script again after every git pull.")

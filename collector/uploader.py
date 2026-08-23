from YukkuriC.minecraft.uploader import *

load_cfg_changelog()

push_file = build_pusher()
push_all('.', push_file)

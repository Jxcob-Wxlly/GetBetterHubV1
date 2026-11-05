#!/usr/bin/env python3
"""Generates res/values*/strings.xml for en, zu, af, xh from ONE table so key sets can never drift.
Usage: gen_strings.py <res_dir>
Format args use positional specifiers (%1$d, %2$s) so translations may reorder them."""
import sys, os
from xml.sax.saxutils import escape

# key | en | zu | af | xh
T = [
# ---- common
("app_name","Get Better Hub","Get Better Hub","Get Better Hub","Get Better Hub"),
("synced","Synced","Kuvumelanisiwe","Gesinkroniseer","Kuvumelanisiwe"),
("syncing","Syncing…","Iyavumelanisa…","Sinkroniseer…","Iyavumelanisa…"),
("offline_pill","Offline","Awuk’ku-inthanethi","Vanlyn af","Awuk’kuntanethi"),
("pending","Pending","Kulindile","Hangend","Kulindile"),
("save","Save","Gcina","Stoor","Gcina"),
("delete","Delete","Susa","Skrap","Cima"),
("cancel","Cancel","Khansela","Kanselleer","Rhoxisa"),
("untitled","Untitled","Akunasihloko","Sonder titel","Akunasihloko"),
("nothing_here_yet","Nothing here yet.","Akukho lutho lapha okwamanje.","Niks hier nie.","Akukho nto apha okwangoku."),
("google_web_client_id_note","","","",""),
# ---- nav
("nav_home","Home","Ikhaya","Tuis","Ikhaya"),
("nav_practice","Practice","Ukuzilolonga","Oefening","Uziqhelisa"),
("nav_mood","Mood","Imizwa","Stemming","Imvakalelo"),
("nav_lyrics","Lyrics","Amagama","Lirieke","Amazwi"),
("nav_journal","Journal","Idayari","Joernaal","Idayari"),
("nav_arena","Arena","Inkundla","Arena","Iqonga"),
# ---- header
("cd_profile","Open profile","Vula iphrofayela","Maak profiel oop","Vula iprofayile"),
("cd_search","Search people","Sesha abantu","Soek mense","Khangela abantu"),
("cd_notifications","Notifications","Izaziso","Kennisgewings","Izaziso"),
("cd_logout","Log out","Phuma","Teken uit","Phuma"),
# ---- auth
("sign_in","Sign in","Ngena","Teken in","Ngena"),
("signing_in","Signing in…","Iyangena…","Teken tans in…","Iyangena…"),
("create_account","Create account","Dala i-akhawunti","Skep rekening","Yenza iakhawunti"),
("creating_account","Creating account…","Iyakha i-akhawunti…","Skep tans rekening…","Iyenza iakhawunti…"),
("email","Email","I-imeyili","E-pos","I-imeyile"),
("password","Password","Iphasiwedi","Wagwoord","Iphasiwedi"),
("display_name","Display name","Igama elibonakalayo","Vertoonnaam","Igama elibonakalayo"),
("continue_with_google","Continue with Google","Qhubeka ne-Google","Gaan voort met Google","Qhubeka ne-Google"),
("google_note","Google sign-in uses secure OAuth with server-side token validation and creates your account automatically the first time.","Ukungena nge-Google kusebenzisa i-OAuth evikelekile futhi kuqinisekiswa kuseva, i-akhawunti yakho idalwa ngokuzenzakalelayo ngesikhathi sokuqala.","Google-aanmelding gebruik veilige OAuth met bedienerkant-tokenvalidering en skep jou rekening outomaties die eerste keer.","Ukungena nge-Google kusebenzisa i-OAuth ekhuselekileyo kwaye i-akhawunti yakho yenziwa ngokuzenzekelayo okokuqala."),
("or","or","noma","of","okanye"),
("new_here","New here? Create an account","Usha lapha? Dala i-akhawunti","Nuut hier? Skep ’n rekening","Intsha apha? Yenza iakhawunti"),
("already_have_account","Already have an account? Sign in","Usunayo i-akhawunti? Ngena","Het jy reeds ’n rekening? Teken in","Sele unayo iakhawunti? Ngena"),
("err_network","Couldn’t reach the server — check your connection.","Asikwazanga ukufinyelela iseva — hlola uxhumano lwakho.","Kon nie die bediener bereik nie — kontroleer jou verbinding.","Ayikwazanga ukufikelela kwiseva — khangela uqhagamshelo lwakho."),
("err_google_cancel","Google sign-in was cancelled or failed.","Ukungena nge-Google kukhanseliwe noma kuhlulekile.","Google-aanmelding is gekanselleer of het misluk.","Ukungena nge-Google kurhoxisiwe okanye kusilele."),
# ---- home
("good_morning","Good morning","Sawubona ekuseni","Goeie môre","Molo ekuseni"),
("good_afternoon","Good afternoon","Sawubona ntambama","Goeie middag","Molo emva kwemini"),
("good_evening","Good evening","Sawubona kusihlwa","Goeie aand","Molo ngokuhlwa"),
("day_streak","Day streak","Izinsuku ezilandelanayo","Dae-reeks","Imihla elandelanayo"),
("level","Level","Izinga","Vlak","Inqanaba"),
("xp","XP","I-XP","XP","I-XP"),
("level_n","Level %1$d","Izinga %1$d","Vlak %1$d","Inqanaba %1$d"),
("xp_progress","%1$d / %2$d XP","%1$d / %2$d XP","%1$d / %2$d XP","%1$d / %2$d XP"),
("jump_back_in","Jump back in","Buyela ngaphakathi","Spring terug in","Buyela ngaphakathi"),
("recent_sessions","Recent sessions","Izikhathi zakamuva","Onlangse sessies","Iiseshoni zakutshanje"),
("sessions","Sessions","Izikhathi","Sessies","Iiseshoni"),
("challenges","Challenges","Izinselelo","Uitdagings","Imiceli mngeni"),
# ---- practice
("practice_roulette","Practice Roulette","Ukuzilolonga Ngethuba","Oefen-roulette","Iroulette yoMthambo"),
("practice_roulette_subtitle","A random drill matched to your instrument and level.","Ukuzilolonga okungahleliwe okufanele insimbi yakho nezinga lakho.","’n Ewekansige oefening wat by jou instrument en vlak pas.","Umthambo ongacwangciswanga ongqamana nesixhobo sakho nenqanaba lakho."),
("drills_in_rotation","Drills in the rotation","Ukuzilolonga okuku-rotation","Oefeninge in die rotasie","Imithambo ekujikelezeni"),
("rotation_hint","Spins draw from the instruments you tick — set more on your Profile.","Ukuphotha kukhetha ezinsimbi ozikhethile — setha ezengeziwe kuphrofayela yakho.","Draaie kies uit die instrumente wat jy afmerk — stel meer in jou Profiel.","Ukujikeleza kukhetha kwizixhobo ozikhethileyo — beka ezongezelelweyo kwiprofayile yakho."),
("main_tag","Main","Eyinhloko","Hoof","Eyintloko"),
("spin_the_roulette","Spin the roulette","Photha ithethe","Draai die roulette","Jikelezisa iroulette"),
("spin_again","Spin again","Photha futhi","Draai weer","Jikelezisa kwakhona"),
("start_timer","Start timer","Qala isikhathi","Begin tydtelling","Qala iwotshi"),
("pause_timer","Pause timer","Misa isikhathi","Pouseer tydtelling","Misa iwotshi"),
("mark_complete","Mark complete","Maka kuphelele","Merk voltooi","Phawula kugqityiwe"),
("suggested_min","Suggested: %1$d min","Okuphakanyisiwe: %1$d min","Voorgestel: %1$d min","Okucetywayo: %1$d min"),
("pct_suggested","%1$d%% of suggested time","%1$d%% wesikhathi esiphakanyisiwe","%1$d%% van voorgestelde tyd","%1$d%% yexesha elicetywayo"),
("time_remaining","%1$s remaining","%1$s esele","%1$s oor","%1$s eseleyo"),
("exercise_progress","Exercise progress","Inqubekela phambili yokuzilolonga","Oefenvordering","Inkqubela yomthambo"),
("sessions_completed","%1$d / %2$d sessions completed","%1$d / %2$d izikhathi eziphelile","%1$d / %2$d sessies voltooi","%1$d / %2$d iiseshoni ezigqityiweyo"),
("time_practised","%1$s practised","%1$s ezilolonge","%1$s geoefen","%1$s eziqhelisile"),
("no_instruments","Tick at least one instrument on your Profile.","Khetha okungenani insimbi eyodwa kuphrofayela yakho.","Merk ten minste een instrument in jou Profiel.","Khetha ubuncinane isixhobo esinye kwiprofayile yakho."),
# instruments / levels / categories
("inst_vocals","Vocals","Ukuhlabelela","Sang","Ukuculela"),
("inst_guitar","Guitar","Isiginci","Kitaar","Isigitaro"),
("inst_piano","Piano","Upiyano","Klavier","Upiyano"),
("inst_drums","Drums","Izigubhu","Dromme","Iingoma"),
("inst_bass","Bass","Ibhesi","Bas","Ibhesi"),
("inst_production","Production","Ukukhiqiza","Produksie","Imveliso"),
("inst_songwriting","Songwriting","Ukubhala izingoma","Liedjieskryf","Ukubhala iingoma"),
("lvl_beginner","Beginner","Umqalisi","Beginner","Umqalisi"),
("lvl_intermediate","Intermediate","Maphakathi","Intermediêr","Ophakathi"),
("lvl_advanced","Advanced","Ochwepheshile","Gevorderd","Oqhubekileyo"),
("cat_warmup","Warm-up","Ukushisa umzimba","Opwarming","Ukufudumeza"),
("cat_scales","Scales","Izikali","Toonlere","Izikali"),
("cat_technique","Technique","Ubuchwepheshe","Tegniek","Ubugcisa"),
("cat_rhythm","Rhythm","Isigqi","Ritme","Isingqi"),
("cat_melody","Melody","Ingoma","Melodie","Umculo"),
("cat_theory","Theory","Ithiyori","Teorie","Ithiyori"),
("cat_ear","Ear training","Ukuqeqeshwa kwendlebe","Gehoortraining","Uqeqesho lwendlebe"),
("cat_creative","Creative","Ubuciko","Kreatief","Ubuchule"),
# ---- mood
("mood_coach","Mood Coach","Umqeqeshi Wemizwa","Stemmingsafrigter","Umqeqeshi Wemvakalelo"),
("mood_coach_subtitle","Tell us where your head is at and we will pick the work.","Sitshele ukuthi ucabangani sizokukhethela umsebenzi.","Vertel ons hoe jy voel en ons kies die werk.","Sixelele ukuba ucinga ntoni size sikukhethele umsebenzi."),
("energy_level_n","Energy level: %1$d","Izinga lamandla: %1$d","Energievlak: %1$d","Inqanaba lamandla: %1$d"),
("get_my_plan","Get my plan","Thola uhlelo lwami","Kry my plan","Fumana icebo lam"),
("mood_energised","Energised","Nginamandla","Energiek","Unamandla"),
("mood_flat","Flat","Ngiphansi","Plat","Ulusizi"),
("mood_anxious","Anxious","Ngiyakhathazeka","Angstig","Uxhalabile"),
("mood_blocked","Blocked","Ngivinjiwe","Geblokkeer","Uvaliwe"),
("mood_frustrated","Frustrated","Ngiyacasuka","Gefrustreerd","Uthukuthele"),
("mood_inspired","Inspired","Ngigqugquzelekile","Geïnspireer","Uphefumlelwe"),
# ---- lyrics
("lyric_sketchpad","Lyric Sketchpad","Ibhukwana Lamagama","Lirieke-sketsblok","Ibhuku Lamazwi"),
("new_draft","New draft","Okusha okuyidrafti","Nuwe konsep","Uyilo olutsha"),
("title_label","Title","Isihloko","Titel","Isihloko"),
("lyrics_label","Lyrics","Amagama engoma","Lirieke","Amazwi engoma"),
("delete_draft","Delete draft","Susa idrafti","Skrap konsep","Cima uyilo"),
# ---- journal
("soundscape_journal","Soundscape Journal","Idayari Yomsindo","Klanklandskap-joernaal","Idayari Yesandi"),
("soundscape_journal_subtitle","Private voice notes. Only you can hear these.","Amanothi emisindo ayimfihlo. Wena kuphela ongawezwa.","Private stemnotas. Net jy kan dit hoor.","Amanqaku olwazi abucala. Nguwe kuphela ongawva."),
("record","Record","Qopha","Neem op","Rekhoda"),
("stop","Stop","Misa","Stop","Yima"),
("title_hint","Title","Isihloko","Titel","Isihloko"),
("untitled_take","Untitled take","Ukuqoshwa okungenasihloko","Opname sonder titel","Irekhodi engenasihloko"),
("play","Play","Dlala","Speel","Dlala"),
# ---- arena
("challenge_arena","Challenge Arena","Inkundla Yezinselelo","Uitdagingsarena","Iqonga Lemiceli Mngeni"),
("challenge_arena_subtitle","Join a challenge, post your evidence, and back other players.","Joyina inselelo, thumela ubufakazi bakho, usekele abanye abadlali.","Sluit by ’n uitdaging aan, plaas jou bewys en ondersteun ander spelers.","Joyina umceli mngeni, thumela ubungqina bakho, uxhase abanye abadlali."),
("tab_feed","Feed","Ukudla","Stroom","Ukutya"),
("tab_challenges","Challenges","Izinselelo","Uitdagings","Imiceli mngeni"),
("tab_leaderboard","Leaderboard","Ibhodi labahamba phambili","Ranglys","Ibhodi yabaphambili"),
("share_a_post","Share a post","Yabelana ngokuthunyelwe","Deel ’n plasing","Yabelana ngokuthunyelweyo"),
("caption","Caption","Incazelo","Onderskrif","Inkcazo"),
("challenge_label","Challenge","Inselelo","Uitdaging","Umceli mngeni"),
("no_challenge","No challenge","Ayikho inselelo","Geen uitdaging","Akukho mceli mngeni"),
("practice_evidence","Practice evidence","Ubufakazi bokuzilolonga","Oefenbewys","Ubungqina bokuziqhelisa"),
("no_linked_session","No linked session","Ayikho isikhathi esixhunyiwe","Geen gekoppelde sessie","Akukho seshoni exhunyiweyo"),
("attach_audio_or_image","Attach audio or image","Namathisela umsindo noma isithombe","Heg klank of beeld aan","Qhoboshela isandi okanye umfanekiso"),
("join_challenge","Join challenge","Joyina inselelo","Sluit by uitdaging aan","Joyina umceli mngeni"),
("joined","Joined","Ujoyinile","Aangesluit","Ujoyine"),
("badges","Badges","Amabheji","Kentekens","Amabheji"),
("likes_comments","%1$d likes   %2$d comments","%1$d ukuthandwa   %2$d amazwana","%1$d patrone   %2$d kommentaar","%1$d ukuthandwa   %2$d izimvo"),
("lv_xp","Lv %1$d · %2$d XP","Izinga %1$d · %2$d XP","Vl %1$d · %2$d XP","Inqanaba %1$d · %2$d XP"),
("xp_reward","+%1$d XP","+%1$d XP","+%1$d XP","+%1$d XP"),
("add_a_comment","Add a comment","Engeza ukuphawula","Voeg ’n opmerking by","Yongeza uluvo"),
# ---- achievements
("achievements","Achievements","Impumelelo","Prestasies","Impumelelo"),
("achievements_unlocked","%1$d/%2$d unlocked","%1$d/%2$d evuliwe","%1$d/%2$d ontsluit","%1$d/%2$d ivuliwe"),
("ach_unlocked","Unlocked","Ivuliwe","Ontsluit","Ivuliwe"),
("ach_locked","Locked: %1$s","Ivaliwe: %1$s","Gesluit: %1$s","Ivaliwe: %1$s"),
("ach_progress","%1$,d / %2$,d %3$s","%1$,d / %2$,d %3$s","%1$,d / %2$,d %3$s","%1$,d / %2$,d %3$s"),
("ach_remaining","%1$,d %2$s remaining","%1$,d %2$s esele","%1$,d %2$s oor","%1$,d %2$s eseleyo"),
("rarity_common","Common","Evamile","Algemeen","Eqhelekileyo"),
("rarity_rare","Rare","Enqabile","Skaars","Engafumanekiyo"),
("rarity_epic","Epic","Enkulu","Epies","Enkulu"),
("rarity_legendary","Legendary","Eyisona","Legendaries","Eyisiqhulo"),
("unit_sessions","sessions","izikhathi","sessies","iiseshoni"),
("unit_drafts","drafts","amadrafti","konsepte","oyilo"),
("unit_streak_days","streak days","izinsuku ezilandelanayo","reeksdae","imihla elandelanayo"),
("unit_comments","comments","amazwana","kommentaar","izimvo"),
("unit_recordings","recordings","ukuqoshwa","opnames","iirekhodi"),
("unit_posts","posts","okuthunyelwe","plasings","iimpapasho"),
("unit_challenges","challenges","izinselelo","uitdagings","imiceli mngeni"),
("unit_words","words","amagama","woorde","amagama"),
("unit_minutes","practice minutes","amaminithi okuzilolonga","oefenminute","imizuzu yokuziqhelisa"),
("unit_xp","XP","i-XP","XP","i-XP"),
("unit_instrument_sessions","%1$s sessions","izikhathi ze-%1$s","%1$s-sessies","iiseshoni ze-%1$s"),
# ---- profile
("profile","Profile","Iphrofayela","Profiel","Iprofayile"),
("change_banner","Change banner","Shintsha isibhengezo","Verander banier","Tshintsha ibhanya"),
("username","Username","Igama lomsebenzisi","Gebruikernaam","Igama lomsebenzisi"),
("view_my_profile","View my profile","Buka iphrofayela yami","Bekyk my profiel","Jonga iprofayile yam"),
("private_account","Private account","I-akhawunti yangasese","Private rekening","Iakhawunti yangasese"),
("private_account_desc","Only followers you approve can see your posts, bio and instruments.","Abalandeli owabavumelayo kuphela abangabona okuthunyelwe, i-bio nezinsimbi zakho.","Net volgers wat jy goedkeur kan jou plasings, bio en instrumente sien.","Abalandeli owabamkelayo kuphela abanokubona iimpapasho, ibhayo nezixhobo zakho."),
("avatar_style","Avatar style","Isitayela se-avatar","Avatar-styl","Isimbo se-avatar"),
("shape_round","Round","Indingilizi","Rond","Ngqukuva"),
("shape_square","Square","Isikwele","Vierkantig","Ikwere"),
("remove_photo","Remove photo","Susa isithombe","Verwyder foto","Susa umfanekiso"),
("who_can_see","Who can see this","Ongakubona lokhu","Wie kan dit sien","Ongakubona oku"),
("who_can_see_desc","Private items are only visible to you. Name, level and XP always show on the leaderboard.","Izinto zangasese zibonakala wena kuphela. Igama, izinga ne-XP kuhlala kubonakala ebhodini labahamba phambili.","Private items is slegs vir jou sigbaar. Naam, vlak en XP wys altyd op die ranglys.","Izinto zangasese zibonakala wena kuphela. Igama, inqanaba ne-XP zihlala zibonakala kwibhodi yabaphambili."),
("bio","Bio","Imininingwane","Bio","Ibhayo"),
("instruments","Instruments","Izinsimbi","Instrumente","Izixhobo"),
("profile_picture","Profile picture","Isithombe sephrofayela","Profielfoto","Umfanekiso weprofayile"),
("earned_badges","Earned badges","Amabheji atholiwe","Verdiende kentekens","Amabheji afunyenweyo"),
("visibility_public","Public","Obala","Openbaar","Esidlangalaleni"),
("visibility_private","Private","Ngasese","Privaat","Wangasese"),
("instrument_label","Instrument","Insimbi","Instrument","Isixhobo"),
("level_label","Level","Izinga","Vlak","Inqanaba"),
("instruments_you_practise","Instruments you practise","Izinsimbi ozilolongayo","Instrumente wat jy oefen","Izixhobo ozaziqhelisayo"),
("primary_tag","Primary","Eyinhloko","Primêr","Eyintloko"),
("edit_profile","Edit profile","Hlela iphrofayela","Wysig profiel","Hlela iprofayile"),
("shared_posts","Shared posts","Okuthunyelwe okwabelwane ngakho","Gedeelde plasings","Iimpapasho ezabelwanayo"),
("no_posts_yet","No posts yet.","Akukho okuthunyelwe okwamanje.","Nog geen plasings nie.","Akukho mpapasho okwangoku."),
("posts","posts","okuthunyelwe","plasings","iimpapasho"),
("followers","followers","abalandeli","volgers","abalandeli"),
("following","following","olandelayo","volg","olandelayo"),
("lv_short","Lv %1$d","Izinga %1$d","Vl %1$d","Inqanaba %1$d"),
("profile_saved","Profile saved.","Iphrofayela igcinwe.","Profiel gestoor.","Iprofayile igciniwe."),
("saved_offline","Saved on this phone — will sync when you’re online.","Kugcinwe kule foni — kuzovumelanisa uma usuku-inthanethi.","Op hierdie foon gestoor — sal sinkroniseer wanneer jy aanlyn is.","Kugcinwe kule fowuni — kuza kuvumelanisa xa uku-intanethi."),
("handle_hint","handle","isibambo","gebruikernaam","igama lomsebenzisi"),
("handle_invalid","Use 3–20 letters, numbers or underscores.","Sebenzisa 3–20 amagama, izinombolo noma ama-underscore.","Gebruik 3–20 letters, syfers of onderstrepe.","Sebenzisa 3–20 iileta, amanani okanye ii-underscore."),
("handle_taken","That @handle is already taken — try another.","Leli @handle selithathiwe — zama elinye.","Daardie @handle is reeds in gebruik — probeer ’n ander een.","Eli @handle selithathiwe — zama elinye."),
("offline_search","Go online to search for people.","Xhumeka ku-inthanethi ukuze usesha abantu.","Gaan aanlyn om mense te soek.","Dibana ne-intanethi ukuze ufune abantu."),
# ---- settings
("settings","Settings","Izilungiselelo","Instellings","Iisetingi"),
("language","Language","Ulimi","Taal","Ulwimi"),
("practice_reminders","Practice reminders","Izikhumbuzo zokuzilolonga","Oefenherinnerings","Izikhumbuzi zokuziqhelisa"),
("practice_reminders_desc","Show a nudge when you have not practised today.","Bonisa isikhumbuzo uma ungazilolongile namuhla.","Wys ’n herinnering as jy nie vandag geoefen het nie.","Bonisa isikhumbuzi xa ungaziqhelisanga namhlanje."),
("follower_alerts","Follower alerts","Izaziso zabalandeli","Volger-kennisgewings","Izaziso zabalandeli"),
("alert_new_followers","New followers","Abalandeli abasha","Nuwe volgers","Abalandeli abatsha"),
("alert_new_followers_desc","When someone starts following you.","Uma othile eqala ukukulandela.","Wanneer iemand jou begin volg.","Xa umntu eqala ukukulandela."),
("alert_follow_requests","Follow requests","Izicelo zokulandela","Volgversoeke","Izicelo zokulandela"),
("alert_follow_requests_desc","When someone asks to follow your private account.","Uma othile ecela ukulandela i-akhawunti yakho yangasese.","Wanneer iemand vra om jou private rekening te volg.","Xa umntu ecela ukulandela iakhawunti yakho yangasese."),
("alert_requests_approved","Requests approved","Izicelo ezamukelwe","Versoeke goedgekeur","Izicelo ezamkelweyo"),
("alert_requests_approved_desc","When someone approves your follow request.","Uma othile enqume isicelo sakho sokulandela.","Wanneer iemand jou volgversoek goedkeur.","Xa umntu amkela isicelo sakho sokulandela."),
("offline_sync","Offline sync","Ukuvumelanisa ngaphandle kwe-inthanethi","Vanlyn sinkronisasie","Ukuvumelanisa ngaphandle kwe-intanethi"),
("status_online","Online","Ku-inthanethi","Aanlyn","Ku-intanethi"),
("status_offline","Offline","Ngaphandle kwe-inthanethi","Vanlyn","Ngaphandle kwe-intanethi"),
("all_saved_cloud","Everything is saved to the cloud.","Konke kugcinwe efwini.","Alles is in die wolk gestoor.","Yonke into igcinwe kwilifu."),
("pending_changes","%1$d changes saved on this phone, waiting to sync.","Izinguquko ezingu-%1$d zigcinwe kule foni, zilinde ukuvumelanisa.","%1$d veranderinge op hierdie foon gestoor, wag om te sinkroniseer.","Utshintsho olu-%1$d lugcinwe kule fowuni, lulindele ukuvumelanisa."),
("last_sync","Last sync %1$s","Ukuvumelanisa kokugcina %1$s","Laaste sinkronisasie %1$s","Ukuvumelanisa kokugqibela %1$s"),
("never_synced","Not synced yet","Akukavumelaniswa","Nog nie gesinkroniseer nie","Akukavumelaniswa"),
("sync_now","Sync now","Vumelanisa manje","Sinkroniseer nou","Vumelanisa ngoku"),
("sign_out","Sign out","Phuma","Teken uit","Phuma"),
("settings_saved","Settings saved.","Izilungiselelo zigcinwe.","Instellings gestoor.","Iisetingi zigciniwe."),
# ---- notifications
("notifications","Notifications","Izaziso","Kennisgewings","Izaziso"),
("no_notifications","No notifications yet.","Azikho izaziso okwamanje.","Nog geen kennisgewings nie.","Azikho izaziso okwangoku."),
("notif_channel_name","Practice reminders & alerts","Izikhumbuzo nezaziso","Oefenherinnerings en kennisgewings","Izikhumbuzi nezaziso"),
("notif_reminder_title","Time to practise","Sekuyisikhathi sokuzilolonga","Tyd om te oefen","Lixesha lokuziqhelisa"),
("notif_reminder_body","You haven’t practised today — spin the roulette and keep your streak alive.","Awukazilolongi namuhla — photha ithethe ugcine uchungechunge lwakho luphila.","Jy het nog nie vandag geoefen nie — draai die roulette en hou jou reeks aan die gang.","Awuziqhelisanga namhlanje — jikelezisa iroulette ugcine uthotho lwakho luphila."),
("notif_new_follower","%1$s started following you.","%1$s uqale ukukulandela.","%1$s het jou begin volg.","%1$s uqalile ukukulandela."),
("notif_follow_request","%1$s asked to follow you.","%1$s ucele ukukulandela.","%1$s het gevra om jou te volg.","%1$s ucele ukukulandela."),
("notif_request_approved","%1$s approved your follow request.","%1$s uvumile isicelo sakho sokulandela.","%1$s het jou volgversoek goedgekeur.","%1$s uvumile isicelo sakho sokulandela."),
("notif_achievement","Achievement unlocked: %1$s","Impumelelo evuliwe: %1$s","Prestasie ontsluit: %1$s","Impumelelo ivuliwe: %1$s"),
("send_test_notification","Send a test notification","Thumela isaziso sokuhlola","Stuur ’n toetskennisgewing","Thumela isaziso sovavanyo"),
("notif_test_body","This is a test — notifications are working.","Lesi isivivinyo — izaziso ziyasebenza.","Dit is ’n toets — kennisgewings werk.","Esi sisivivinyo — izaziso ziyasebenza."),
("notif_allow_prompt","Allow notifications so we can remind you to practise.","Vumela izaziso ukuze sikukhumbuze ukuzilolonga.","Laat kennisgewings toe sodat ons jou kan herinner om te oefen.","Vumela izaziso ukuze sikukhumbuze ukuziqhelisa."),
# ---- search / follow
("search_hint","Search by name or @handle","Sesha ngegama noma @isibambo","Soek op naam of @gebruikernaam","Khangela ngegama okanye @igama"),
("search_needs_online","Searching people needs a connection.","Ukusesha abantu kudinga uxhumano.","Om mense te soek het ’n verbinding nodig.","Ukukhangela abantu kufuna uqhagamshelo."),
("no_results","No people found.","Akutholakalanga muntu.","Geen mense gevind nie.","Akukho bantu bafunyenweyo."),
("follow","Follow","Landela","Volg","Landela"),
("unfollow","Unfollow","Yeka ukulandela","Hou op volg","Yeka ukulandela"),
("requested","Requested","Kucelwe","Versoek","Kucelwe"),
("approve","Approve","Vuma","Keur goed","Vuma"),
("decline","Decline","Yenqaba","Weier","Yala"),
# ---- misc / permissions / errors
("mic_denied","Microphone permission is needed to record.","Imvume yemakrofoni iyadingeka ukuqopha.","Mikrofoontoestemming is nodig om op te neem.","Imvume yemayikhrofoni iyafuneka ukurekhoda."),
("logout_confirm","Sign out of Get Better Hub?","Phuma ku-Get Better Hub?","Teken uit by Get Better Hub?","Phuma kwi-Get Better Hub?"),
]

LANGS = [("values",1),("values-zu",2),("values-af",3),("values-xh",4)]

def esc(s):
    # XML-escape, then Android-escape quotes; a leading "@" would be read as a resource ref.
    s = escape(s).replace("'", "\\'").replace('"', '\\"')
    return "\\" + s if s.startswith("@") else s

def main(res):
    keys = [t[0] for t in T if t[0] != "google_web_client_id_note"]
    assert len(keys) == len(set(keys)), "duplicate keys"
    for folder, idx in LANGS:
        d = os.path.join(res, folder); os.makedirs(d, exist_ok=True)
        out = ['<?xml version="1.0" encoding="utf-8"?>', '<resources>']
        if folder == "values":
            out.append('    <!-- Web client ID of the Google Cloud OAuth client (same one the backend uses as Google:ClientId). -->')
            out.append('    <string name="google_web_client_id" translatable="false">REPLACE_WITH_YOUR_GOOGLE_OAUTH_WEB_CLIENT_ID</string>')
        for t in T:
            if t[0] == "google_web_client_id_note": continue
            val = t[idx]
            assert val, f"missing {folder}:{t[0]}"
            out.append(f'    <string name="{t[0]}">{esc(val)}</string>')
        out.append('</resources>')
        open(os.path.join(d, "strings.xml"), "w", encoding="utf-8").write("\n".join(out) + "\n")
    print(f"{len(keys)} keys x {len(LANGS)} languages written")

if __name__ == "__main__":
    main(sys.argv[1])

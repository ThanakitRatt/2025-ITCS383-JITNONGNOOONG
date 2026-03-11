#!/usr/bin/env python3
BASE = 'https://images.unsplash.com/photo-'
FIT  = '&fit=crop&auto=format'

M = {
    # Restaurants logos (400x400)
    'picsum.photos/seed/thai-street-logo/400/400':    BASE+'1467003909585-2f8a72700288?w=400&h=400'+FIT,
    'picsum.photos/seed/sushi-master-logo/400/400':   BASE+'1553621042-f6e147245754?w=400&h=400'+FIT,
    'picsum.photos/seed/italiano-logo/400/400':       BASE+'1565299624946-b28f40a0ae38?w=400&h=400'+FIT,
    'picsum.photos/seed/big-burger-logo/400/400':     BASE+'1568901346375-23c9450c58cd?w=400&h=400'+FIT,
    'picsum.photos/seed/ramen-bar-logo/400/400':      BASE+'1569050467447-ce54b3bbc37d?w=400&h=400'+FIT,
    # Restaurants covers (1200x500)
    'picsum.photos/seed/thai-street-cover/1200/500':  BASE+'1562802378-063ec186a863?w=1200&h=500'+FIT,
    'picsum.photos/seed/sushi-master-cover/1200/500': BASE+'1617196035154-1e7e6e28b0db?w=1200&h=500'+FIT,
    'picsum.photos/seed/italiano-cover/1200/500':     BASE+'1574071318508-1cdbab80d002?w=1200&h=500'+FIT,
    'picsum.photos/seed/big-burger-cover/1200/500':   BASE+'1550547660-d9450f859349?w=1200&h=500'+FIT,
    'picsum.photos/seed/ramen-bar-cover/1200/500':    BASE+'1582878826629-33b175fbe285?w=1200&h=500'+FIT,
    # Categories (400x300)
    'picsum.photos/seed/cat-noodles/400/300':      BASE+'1569050467447-ce54b3bbc37d?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-curry/400/300':        BASE+'1455619452474-d2be8b1e70cd?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-rice/400/300':         BASE+'1603133872878-684f208fb84b?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-dessert/400/300':      BASE+'1558618666-fcd25c85cd64?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-sushi/400/300':        BASE+'1553621042-f6e147245754?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-sashimi/400/300':      BASE+'1617196033183-421062a1103b?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-hotjp/400/300':        BASE+'1563245372-f21724e3856d?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-jpdrinks/400/300':     BASE+'1509042239860-f550ce710b93?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-pizza/400/300':        BASE+'1565299624946-b28f40a0ae38?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-pasta/400/300':        BASE+'1551892374-ecf8754cf744?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-salad/400/300':        BASE+'1512621776951-a57141f2eefd?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-italdrinks/400/300':   BASE+'1497534446932-c925b458314e?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-burgers/400/300':      BASE+'1568901346375-23c9450c58cd?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-fries/400/300':        BASE+'1573080496219-bb964701c2a8?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-shakes/400/300':       BASE+'1572490122747-3e9f7f1614af?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-ramen/400/300':        BASE+'1569050467447-ce54b3bbc37d?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-ramensides/400/300':   BASE+'1563245372-f21724e3856d?w=400&h=300'+FIT,
    'picsum.photos/seed/cat-ramendrinks/400/300':  BASE+'1547592166-23ac45744acd?w=400&h=300'+FIT,
    # Menu items - Bangkok Street Food
    'picsum.photos/seed/item-pad-thai/400/300':        BASE+'1559314809-0d155014e29e?w=400&h=300'+FIT,
    'picsum.photos/seed/item-pad-see-ew/400/300':      BASE+'1527515637462-cff94ebb84ec?w=400&h=300'+FIT,
    'picsum.photos/seed/item-kee-mao/400/300':         BASE+'1533622597524-a1215e26c0a2?w=400&h=300'+FIT,
    'picsum.photos/seed/item-boat-noodles/400/300':    BASE+'1547592166-23ac45744acd?w=400&h=300'+FIT,
    'picsum.photos/seed/item-tom-yum/400/300':         BASE+'1455619452474-d2be8b1e70cd?w=400&h=300'+FIT,
    'picsum.photos/seed/item-tom-kha/400/300':         BASE+'1476718406336-bb5a9690ee2a?w=400&h=300'+FIT,
    'picsum.photos/seed/item-green-curry/400/300':     BASE+'1596797882599-15b1c6ce4ddf?w=400&h=300'+FIT,
    'picsum.photos/seed/item-massaman/400/300':        BASE+'1455619452474-d2be8b1e70cd?w=400&h=300'+FIT,
    'picsum.photos/seed/item-khao-pad/400/300':        BASE+'1603133872878-684f208fb84b?w=400&h=300'+FIT,
    'picsum.photos/seed/item-khao-man-gai/400/300':    BASE+'1504674900247-0877df9cc836?w=400&h=300'+FIT,
    'picsum.photos/seed/item-kra-pao/400/300':         BASE+'1562802378-063ec186a863?w=400&h=300'+FIT,
    'picsum.photos/seed/item-mango-sticky/400/300':    BASE+'1568718119408-99a3d08e2e0b?w=400&h=300'+FIT,
    'picsum.photos/seed/item-thai-tea/400/300':        BASE+'1558618666-fcd25c85cd64?w=400&h=300'+FIT,
    'picsum.photos/seed/item-pandan-jelly/400/300':    BASE+'1488477181771-4695d2499bef?w=400&h=300'+FIT,
    # Menu items - Sushi Master
    'picsum.photos/seed/item-salmon-roll/400/300':     BASE+'1617196035154-1e7e6e28b0db?w=400&h=300'+FIT,
    'picsum.photos/seed/item-california-roll/400/300': BASE+'1553621042-f6e147245754?w=400&h=300'+FIT,
    'picsum.photos/seed/item-spicy-tuna/400/300':      BASE+'1617196033322-c0c22c52caaf?w=400&h=300'+FIT,
    'picsum.photos/seed/item-dragon-roll/400/300':     BASE+'1617196033423-3ac59e3aedf4?w=400&h=300'+FIT,
    'picsum.photos/seed/item-salmon-sashimi/400/300':  BASE+'1617196033183-421062a1103b?w=400&h=300'+FIT,
    'picsum.photos/seed/item-tuna-sashimi/400/300':    BASE+'1617196035303-f6ad4f09e4e2?w=400&h=300'+FIT,
    'picsum.photos/seed/item-mixed-sashimi/400/300':   BASE+'1580822184713-fc5ded9f8d0f?w=400&h=300'+FIT,
    'picsum.photos/seed/item-gyoza-sushi/400/300':     BASE+'1563245372-f21724e3856d?w=400&h=300'+FIT,
    'picsum.photos/seed/item-edamame/400/300':         BASE+'1543362906-acfc16c67564?w=400&h=300'+FIT,
    'picsum.photos/seed/item-tempura/400/300':         BASE+'1632206000219-0d15f20bf35d?w=400&h=300'+FIT,
    'picsum.photos/seed/item-miso-soup/400/300':       BASE+'1547592166-23ac45744acd?w=400&h=300'+FIT,
    'picsum.photos/seed/item-sake/400/300':            BASE+'1608270586263-f0c14b71d534?w=400&h=300'+FIT,
    'picsum.photos/seed/item-ocha/400/300':            BASE+'1509042239860-f550ce710b93?w=400&h=300'+FIT,
    'picsum.photos/seed/item-ramune/400/300':          BASE+'1543363006-09a7ac5de37a?w=400&h=300'+FIT,
    # Menu items - Italiano Pizzeria
    'picsum.photos/seed/item-margherita/400/300':        BASE+'1565299624946-b28f40a0ae38?w=400&h=300'+FIT,
    'picsum.photos/seed/item-pepperoni/400/300':         BASE+'1628840042765-356cda07504e?w=400&h=300'+FIT,
    'picsum.photos/seed/item-quattro-formaggi/400/300':  BASE+'1571997473849-4fa1c2234c7a?w=400&h=300'+FIT,
    'picsum.photos/seed/item-bbq-chicken-pizza/400/300': BASE+'1574071318508-1cdbab80d002?w=400&h=300'+FIT,
    'picsum.photos/seed/item-carbonara/400/300':         BASE+'1551892374-ecf8754cf744?w=400&h=300'+FIT,
    'picsum.photos/seed/item-arrabbiata/400/300':        BASE+'1563379926898-05f4575a45d8?w=400&h=300'+FIT,
    'picsum.photos/seed/item-alfredo/400/300':           BASE+'1546069901-ba9599a7e63c?w=400&h=300'+FIT,
    'picsum.photos/seed/item-lasagna/400/300':           BASE+'1574672280600-4accfa5b6f98?w=400&h=300'+FIT,
    'picsum.photos/seed/item-caesar/400/300':            BASE+'1551248429-40975aa4de74?w=400&h=300'+FIT,
    'picsum.photos/seed/item-bruschetta/400/300':        BASE+'1572695157366-5e585ab2b69f?w=400&h=300'+FIT,
    'picsum.photos/seed/item-focaccia/400/300':          BASE+'1585515656973-b0b67f0c7ac0?w=400&h=300'+FIT,
    'picsum.photos/seed/item-pellegrino/400/300':        BASE+'1543179116-13c3b9ce2f6e?w=400&h=300'+FIT,
    'picsum.photos/seed/item-lemonade/400/300':          BASE+'1497534446932-c925b458314e?w=400&h=300'+FIT,
    'picsum.photos/seed/item-tiramisu-shake/400/300':    BASE+'1572490122747-3e9f7f1614af?w=400&h=300'+FIT,
    # Menu items - Big Burger House
    'picsum.photos/seed/item-cheeseburger/400/300':      BASE+'1568901346375-23c9450c58cd?w=400&h=300'+FIT,
    'picsum.photos/seed/item-bbq-burger/400/300':        BASE+'1550547660-d9450f859349?w=400&h=300'+FIT,
    'picsum.photos/seed/item-chicken-burger/400/300':    BASE+'1606755962773-d324e0a13086?w=400&h=300'+FIT,
    'picsum.photos/seed/item-mushroom-burger/400/300':   BASE+'1571091718767-18b5b1457add?w=400&h=300'+FIT,
    'picsum.photos/seed/item-double-smash/400/300':      BASE+'1594212699903-ec8a3eca50f5?w=400&h=300'+FIT,
    'picsum.photos/seed/item-shoestring-fries/400/300':  BASE+'1573080496219-bb964701c2a8?w=400&h=300'+FIT,
    'picsum.photos/seed/item-cheese-fries/400/300':      BASE+'1633356122544-f134324a6cee?w=400&h=300'+FIT,
    'picsum.photos/seed/item-onion-rings/400/300':       BASE+'1639024471283-03518883512d?w=400&h=300'+FIT,
    'picsum.photos/seed/item-coleslaw/400/300':          BASE+'1515888844796-5a7a33a65773?w=400&h=300'+FIT,
    'picsum.photos/seed/item-choc-shake/400/300':        BASE+'1572490122747-3e9f7f1614af?w=400&h=300'+FIT,
    'picsum.photos/seed/item-straw-shake/400/300':       BASE+'1519225421980-d8b18fa4c5e5?w=400&h=300'+FIT,
    'picsum.photos/seed/item-vanilla-shake/400/300':     BASE+'1570197788417-0e82375c9371?w=400&h=300'+FIT,
    'picsum.photos/seed/item-sparkling-lem/400/300':     BASE+'1497534446932-c925b458314e?w=400&h=300'+FIT,
    # Menu items - Ramen Noodle Bar
    'picsum.photos/seed/item-tonkotsu/400/300':      BASE+'1569050467447-ce54b3bbc37d?w=400&h=300'+FIT,
    'picsum.photos/seed/item-miso-ramen/400/300':    BASE+'1591814468924-caf88d1232e1?w=400&h=300'+FIT,
    'picsum.photos/seed/item-shoyu-ramen/400/300':   BASE+'1582878826629-33b175fbe285?w=400&h=300'+FIT,
    'picsum.photos/seed/item-vegan-ramen/400/300':   BASE+'1547592166-23ac45744acd?w=400&h=300'+FIT,
    'picsum.photos/seed/item-chashu/400/300':        BASE+'1546069901-ba9599a7e63c?w=400&h=300'+FIT,
    'picsum.photos/seed/item-ajitsuke-egg/400/300':  BASE+'1607920578393-5f3dc6e4efa5?w=400&h=300'+FIT,
    'picsum.photos/seed/item-ramen-gyoza/400/300':   BASE+'1563245372-f21724e3856d?w=400&h=300'+FIT,
    'picsum.photos/seed/item-karaage/400/300':       BASE+'1562802378-063ec186a863?w=400&h=300'+FIT,
    'picsum.photos/seed/item-calpis/400/300':        BASE+'1543363006-09a7ac5de37a?w=400&h=300'+FIT,
    'picsum.photos/seed/item-mugicha/400/300':       BASE+'1509042239860-f550ce710b93?w=400&h=300'+FIT,
    'picsum.photos/seed/item-jp-coffee/400/300':     BASE+'1509042239860-f550ce710b93?w=400&h=300'+FIT,
}

path = 'docker/seed-data.sql'
with open(path, 'r') as f:
    content = f.read()

before_count = content.count('picsum.photos')
for old, new in M.items():
    content = content.replace(old, new)
after_count = content.count('picsum.photos')

with open(path, 'w') as f:
    f.write(content)

print(f"Replaced {before_count - after_count} picsum URLs. Remaining: {after_count}")

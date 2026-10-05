-- Seed members at tier-relevant lifetime point balances
-- SPEC: mem_blue 24999 | mem_silver 25000 | mem_gold 50000 | mem_plat 100000 | mem_whale 500000
INSERT INTO MemberEntity(memberId, lifetimePoints) VALUES ('mem_blue',    24999);
INSERT INTO MemberEntity(memberId, lifetimePoints) VALUES ('mem_silver',  25000);
INSERT INTO MemberEntity(memberId, lifetimePoints) VALUES ('mem_gold',     5000);
INSERT INTO MemberEntity(memberId, lifetimePoints) VALUES ('mem_plat',   100000);
INSERT INTO MemberEntity(memberId, lifetimePoints) VALUES ('mem_whale',  500000);

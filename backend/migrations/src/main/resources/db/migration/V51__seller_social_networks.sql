-- The accounts of the shop on the social networks (Facebook, Instagram, X, TikTok, YouTube, Twitch), as web
-- addresses: the administrators give them, each one optional, with each new version of the seller, and the
-- footer of the shop shows the ones that are given. The versions written before have none (NULL): they cannot be
-- filled without inventing them, and none is mandatory
ALTER TABLE seller ADD COLUMN facebook_url VARCHAR(255);
ALTER TABLE seller ADD COLUMN instagram_url VARCHAR(255);
ALTER TABLE seller ADD COLUMN x_url VARCHAR(255);
ALTER TABLE seller ADD COLUMN tiktok_url VARCHAR(255);
ALTER TABLE seller ADD COLUMN youtube_url VARCHAR(255);
ALTER TABLE seller ADD COLUMN twitch_url VARCHAR(255);

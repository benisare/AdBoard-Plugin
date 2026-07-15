# AdBoard — a Paper plugin for `/ad`

Lets players post a short ad for a service they offer (e.g. "Selling enchanted
tools, visit shop at /warp market") and lets everyone else browse those ads.

## Commands

| Command | Who | What it does |
|---|---|---|
| `/ad` | everyone | Opens the GUI browser (paginated, 45 ads/page) |
| `/ad post <message>` | everyone | Posts or replaces your ad |
| `/ad remove` | everyone | Removes your own ad |
| `/ad browse` | everyone | Same as `/ad` |
| `/ad list` | everyone (works from console too) | Plain-text listing of all ads |
| `/ad reload` | admin | Reloads config.yml and ads.yml |
| `/ad clear <player>` | admin | Removes another player's ad |

Each player can only have **one active ad** — posting again replaces the old one.

## Permissions

- `adboard.post` (default: true)
- `adboard.remove` (default: true)
- `adboard.browse` (default: true)
- `adboard.admin` (default: op) — reload / clear other players' ads

## Config (`config.yml`)

```yaml
max-message-length: 100   # max characters per ad
ad-expiry-hours: 0        # 0 = ads never expire; set e.g. 72 to auto-remove after 3 days
```

## Storage

Ads are stored in `plugins/AdBoard/ads.yml`, keyed by player UUID, so this
survives name changes and server restarts.

## Building the jar

This project wasn't compiled in this environment (no internet access here to
pull the Paper API from Maven Central), so you'll need to build it yourself —
takes about 30 seconds with an internet connection:

1. Install a JDK (21 or newer works for *compiling* — see note below) and
   Maven if you don't have them.
2. From this folder, run:
   ```
   mvn clean package
   ```
3. The built jar will be at `target/adboard-1.0.0.jar`.
4. Drop that jar into your Paper server's `plugins/` folder and restart (or
   `/reload` — though a full restart is safer).

If you'd rather not install Maven, open this folder as a Maven project in
IntelliJ IDEA or VS Code (with the Java extension pack) and use the built-in
"Maven > package" action instead — same result.

This targets Paper **26.2**. Note the version scheme changed in 2026 to
`year.drop` (26.2 = 2nd drop of 2026) instead of `1.x.x`. Two things to know:

- **Running the server itself needs Java 25** (Paper 26.1+ requires it) —
  that's about the *server*, not this build. Make sure your server's Java
  runtime is 25+.
- **Compiling this plugin** only needs JDK 21+ (the pom targets 21 for
  broader compatibility with your dev machine); the resulting bytecode runs
  fine on the server's Java 25 JVM. If you'd rather match exactly, bump
  `maven.compiler.source`/`target` in `pom.xml` to `25`.
- The `paper-api` dependency in `pom.xml` uses a version range
  (`[26.2.build,)`) so Maven automatically grabs the latest available 26.2
  build. If you want a pinned, reproducible build instead, replace it with
  an exact version like `26.2.build.NN-stable` (check
  https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/
  for the latest build number).

If your server is actually on a different version than 26.2, update the
`paper-api` version in `pom.xml` and `api-version` in `plugin.yml` to match.

## Notes / possible extensions

- Ads are plain text right now (no color codes) to keep things simple and
  avoid players spamming with color/formatting — let me know if you'd like
  `&`-color code support added.
- No cost to post — if you want posting to cost money (Vault integration) or
  be rate-limited (e.g. one post per hour), that's a straightforward addition.
- Right now clicking an ad in the GUI does nothing extra — could easily be
  wired up to message the advertiser or teleport to their shop warp.

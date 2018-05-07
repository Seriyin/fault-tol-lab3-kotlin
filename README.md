# Exercise in fault tolerance - Passive Replication #

- Take several servers with the same single bank account.

- Expects multiple clients asking for balance or movements over TCP.

- Collect all balances of individual clients that make transactions on behalf of that account.

- Sum and collect a total balance.

- Check if summed balance matches account balance.

- Use Spread toolkit to ensure total ordered + uniform reliable multicasts.

- Have a single leader server, passively replicates to N replicas.
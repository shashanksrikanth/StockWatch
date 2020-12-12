# StockWatch
This is an Android app that allows the user to display a sorted list of selected stocks, alongside stock information (company name, current price, daily price change amount, and price percent change).

## Features
1. The user can add a stock by looking it up by abbreviation.
2. Once added, the user is shown the stock's information (as mentioned above).
3. If the user clicks on a stock, the app will take the user to a stock website that shows more details about the clicked stock.
4. The stocks the user adds are saved in a JSON file in local storage, and then read from in startup.
5. Pulling down on the screen will refresh stock data.

## Technical concepts used
1. Internet access and permissions
2. RecyclerView
3. Option-Menus
4. Multi-threading via runnables
5. Swipe-Refresh layout
6. Implied intents
7. JSON read/write

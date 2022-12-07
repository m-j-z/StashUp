# StashUp

#### A more updated README will exist on the github

[Project Pitch](https://www.youtube.com/watch?v=kdr1QGdF5wQ&feature=youtu.be)

[Show and Tell 1](https://www.youtube.com/watch?v=kdr1QGdF5wQ&feature=youtu.be)

[Show and Tell 2](https://youtu.be/P-O299EfgAY)

<details>
  <summary>Team Effort Breakdown</summary>
  
  <h3>Scott Luu</h3>
  <pre>
  Brainstormed ideas/tasks and coordinated tasks among members
  Implemented Expenditure page’s design and features,  including:
    Data extraction from Firebase for:
    City & Country’s Avg. Spending
    Determine a city’s High vs. Low Season
    A ListView with a custom BaseAdapter to displays % of transactions’ category
    Utilize Michael’s Country/City Selection dropdown tool to get information of countries and cities for the above methods 

  </pre>
  
  <h3>Tejeshwar Singh Multani</h3>
  <pre>
  Implemented the home page design
  Implemented the transactions list in home page that displays all the transaction data the user has from the Firebase database
  Making sure that all requirements of show and tells, final presentations are met, and what we are presenting corrects depicts our app
  </pre>
  
  <h3>Michael Zhu</h3>
  <pre>
  Core components:
    Created AuthViewModel, AuthViewModelFactory and AuthRepository that interacts with Firebase Authentication to verify/register users
    Currency type changer using a Currency Library
    Extract countries and cities data from a JSON file
    Created Transaction ViewModel, TransactionViewModelFactory, and Transaction Repository with methods to store and retrieve data from Firebase with MVVM architecture
    Custom adapter to display transactions in a RecyclerView
    Custom adapter to display persons in a ListView
  Authentication implementations such as:
    Login and Register functionality
    Forgot Password functionality
    Profile update methods functionality
  Transactions implementations such as:
    Transaction creation
    Transaction list which displays all transactions owned by or shared with the user
    Feature to edit transaction details and save to Firebase
    Feature to fetch transactions with QR codes or by an identifier
  Display homepage total balance
  </pre>
  
</details>

[MVVM and Thread Design Diagram](https://docs.google.com/presentation/d/1CjuodK9SNi6eGaUnif9J1mQivT2SN5kppOBAr81Drfg/edit?usp=sharing)

[Final Presentation Video](https://youtu.be/bNrns7LS2Sk)

[APK here](https://github.com/m-j-z/StashUp/releases/download/v0.01/StashUp.apk)

[Source Code (Release)](https://github.com/m-j-z/StashUp/releases/tag/v0.01)

[Source Code (GitHub)](https://github.com/m-j-z/StashUp)

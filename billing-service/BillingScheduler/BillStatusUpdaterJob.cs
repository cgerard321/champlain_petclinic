using Quartz;
using System.Threading.Tasks;
using System;
using MongoDB.Driver;
using MongoDB.Bson;

namespace BillingScheduler
{
    public class BillStatusUpdaterJob : IJob
    {
        public async Task Execute(IJobExecutionContext context)
        {
            // MongoDB connection details (use env vars in production)
            var mongoUser = Environment.GetEnvironmentVariable("MONGO_USER") ?? "root";
            var mongoPass = Environment.GetEnvironmentVariable("MONGO_PASS") ?? "password";
            var mongoHost = Environment.GetEnvironmentVariable("MONGO_HOST") ?? "mongo-billing";
            var mongoPort = Environment.GetEnvironmentVariable("MONGO_PORT") ?? "27017";
            var mongoDb = Environment.GetEnvironmentVariable("MONGO_DB") ?? "billings";
            var mongoAuthDb = Environment.GetEnvironmentVariable("MONGO_AUTH_DB") ?? "admin";

            var connectionString = $"mongodb://{mongoUser}:{mongoPass}@{mongoHost}:{mongoPort}/{mongoDb}?authSource={mongoAuthDb}";
            var client = new MongoClient(connectionString);
            var database = client.GetDatabase(mongoDb);
            var bills = database.GetCollection<BsonDocument>("bills");

            // Find overdue bills
            var filter = Builders<BsonDocument>.Filter.Eq("status", "OVERDUE");
            var overdueBills = await bills.Find(filter).ToListAsync();

            Console.WriteLine($"[{DateTime.Now}] Found {overdueBills.Count} overdue bills.");

            // Update bills as needed
            foreach (var bill in overdueBills)
            {
                // TODO: Add your update logic here
                Console.WriteLine($"Updating bill with ID: {bill["_id"]}");
                // Example: await bills.UpdateOneAsync(...);
            }

            Console.WriteLine($"[{DateTime.Now}] BillStatusUpdaterJob: Completed.");
        }
    }
}

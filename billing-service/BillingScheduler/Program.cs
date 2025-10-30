using System;
using System.Threading.Tasks;

namespace BillingScheduler
{
    public class Program
    {
        public static async Task Main(string[] args)
        {
            Console.WriteLine("Service started.");
            await SchedulerStartup.StartSchedulerAsync();
            await Task.Delay(-1); // Keep service running
            Console.WriteLine("Service terminated.");
        }
    }
}

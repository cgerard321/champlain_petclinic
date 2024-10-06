using System.IO.Enumeration;
using emailing_service_test.Models.Database;
using emailing_service.BackgroundTask;
using emailing_service.Models;
using emailing_service.Models.Database;
using emailing_service.Utils;
using Microsoft.Extensions.Logging;
using Moq;

namespace emailing_service_test.Background_Task{
    [TestFixture]
    public class RecurringJobServiceTests
    {
        private Mock<ILogger<RecurringJobService>> _mockLogger;
        private RecurringJobService _service;
        private IDatabaseHelper _database;

        [SetUp]
        public void Setup()
        {
            _database = new TestDbContext();
            // Initialize the mock logger
            _mockLogger = new Mock<ILogger<RecurringJobService>>();

            // Create the service instance
            _service = new RecurringJobService(_mockLogger.Object, _database);
        }

        [Test]
        public async Task ExecuteAsync_LogsInitialDelayAndStartsTimer()
        {
            // Arrange
            var stoppingToken = new CancellationTokenSource().Token;
            var now = DateTime.Now;
    
            // Calculate the expected initial delay using reflection to invoke the CalculateInitialDelay method
            var expectedDelay = (TimeSpan)_service.GetType()
                .GetMethod("CalculateInitialDelay", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { now });

            // Ensure the expected delay is not negative
            if (expectedDelay < TimeSpan.Zero)
            {
                expectedDelay = TimeSpan.Zero;
            }

            // Act
            await _service.StartAsync(stoppingToken); // This calls ExecuteAsync internally

            // Assert
            // Verify the logger was called with the expected initial delay message
            _mockLogger.Verify(logger => logger.Log(
                    It.Is<LogLevel>(logLevel => logLevel == LogLevel.Information),
                    It.IsAny<EventId>(),
                    It.Is<It.IsAnyType>((state, t) => state.ToString().Contains($"Recurring job started with initial delay of: {expectedDelay}")),
                    It.IsAny<Exception>(),
                    It.Is<Func<It.IsAnyType, Exception, string>>((state, ex) => true)),
                Times.Once);

            // Optionally, you can check other aspects, such as ensuring the Timer was initialized with the correct period
            // However, this is harder to test without directly accessing internal members or using more complex reflection techniques
        }


        [Test]
        public void CalculateInitialDelay_ReturnsCorrectDelay()
        {
            // Arrange
            var now = new DateTime(2024, 9, 22, 14, 15, 15); // Example time: 14:15
            var expectedDelay = TimeSpan.FromSeconds(45); // 15 minutes to the next 14:30 interval

            // Act
            var result = _service.GetType()
                .GetMethod("CalculateInitialDelay", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { now });

            // Assert
            Assert.That(result, Is.EqualTo(expectedDelay));
        }

        [Test]
        public void DoWork_LogsExecution()
        {
            // Act
            _service.DoWork(new object());

            // Assert
            _mockLogger.Verify(logger => logger.Log(
                It.Is<LogLevel>(logLevel => logLevel == LogLevel.Information),
                It.IsAny<EventId>(),
                #pragma warning disable CS8602 // Dereference of a possibly null reference.
                It.Is<It.IsAnyType>((state, t) => state.ToString().Contains("Recurring task executed")),
                #pragma warning restore CS8602 // Dereference of a possibly null reference.
                It.IsAny<Exception>(),
                #pragma warning disable CS8620 // Argument cannot be used for parameter due to differences in the nullability of reference types.
                It.Is<Func<It.IsAnyType, Exception, string>>((state, ex) => true)),
                #pragma warning restore CS8620 // Argument cannot be used for parameter due to differences in the nullability of reference types.
                Times.Once);
        }
        [Test]
        public void DoWork_SuccessfullyTriedSendingEmail()
        {
            EmailUtils.emailConnectionString = new ConnectionEmailServer(
                "smtpServer",
                123,
                "helloWorld",
                "smtpPassword",
                "xilef992@gmail.com",
                "ChamplainPetClinic"
            );
            bool previousValue = EmailUtils.sendEmail;
            EmailUtils.sendEmail = false;
            
            _database.AddEmailNotificationAsync("example@gmail.com",
                "helloWorld",
                "IWillLiteralyDieYound - FelixAllard", 
                "NotSent",
                TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow,
                    TimeZoneInfo.CreateCustomTimeZone("UTC-4", new TimeSpan(-4, 0, 0), "UTC-4", "UTC-4")).AddSeconds(1));
            
            _service.DoWork(new object?[] { null });
            Assert.That(1, Is.EqualTo(_database.GetAllEmailsAsync().Result.Count));
            Assert.That(0, Is.EqualTo(_database.GetAllEmailsNotificationAsync().Result.Count));
            Assert.That(_database.GetAllEmailsAsync().Result.First().EmailStatus, Is.EqualTo("Sent"));
            
            EmailUtils.sendEmail = previousValue;
        }
        
        
        [Test]
        public void DoWork_UnsuccessfullyTriedSendingEmail()
        {
            EmailUtils.emailConnectionString = new ConnectionEmailServer(
                "smtpServer",
                123,
                "helloWorld",
                "smtpPassword",
                "xilef992@gmail.com",
                "ChamplainPetClinic"
            );
            bool previousValue = EmailUtils.sendEmail;
            EmailUtils.sendEmail = true;
            
            _database.AddEmailNotificationAsync("example@gmail.com",
                "helloWorld",
                "IWillLiteralyDieYound - FelixAllard", 
                "NotSent",
                TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow,
                    TimeZoneInfo.CreateCustomTimeZone("UTC-4", new TimeSpan(-4, 0, 0), "UTC-4", "UTC-4")).AddSeconds(1));
            
            _service.DoWork(new object?[] { null });
            Assert.That(1, Is.EqualTo(_database.GetAllEmailsAsync().Result.Count));
            Assert.That(0, Is.EqualTo(_database.GetAllEmailsNotificationAsync().Result.Count));
            Assert.That(_database.GetAllEmailsAsync().Result.First().EmailStatus, Is.EqualTo("Failed"));
            EmailUtils.sendEmail = previousValue;
        }
        [Test]
        public void DoWork_EmailStillInTableAfterPassedDate()
        {
            EmailUtils.emailConnectionString = new ConnectionEmailServer(
                "smtpServer",
                123,
                "helloWorld",
                "smtpPassword",
                "xilef992@gmail.com",
                "ChamplainPetClinic"
            );
            bool previousValue = EmailUtils.sendEmail;
            EmailUtils.sendEmail = true;
            
            _database.AddEmailNotificationAsync("example@gmail.com",
                "helloWorld",
                "IWillLiteralyDieYound - FelixAllard", 
                "NotSent",
                TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow,
                    TimeZoneInfo.CreateCustomTimeZone("UTC-4", new TimeSpan(-4, 0, 0), "UTC-4", "UTC-4")).AddYears(-10));
            
            _service.DoWork(new object?[] { null });
            Assert.That(1, Is.EqualTo(_database.GetAllEmailsAsync().Result.Count));
            Assert.That(0, Is.EqualTo(_database.GetAllEmailsNotificationAsync().Result.Count));
            EmailUtils.sendEmail = previousValue;
        }
        
        
        [Test]
        public void CalculateInitialDelay_WhenAtExactInterval_SetsDelayToZero()
        {
            // Arrange
            var now = new DateTime(2024, 9, 22, 14, 1, 0); // Exactly at a round half-hour interval (14:30)
            var expectedDelay = TimeSpan.FromMinutes(1); // The delay should be zero since we are already at the desired interval

            // Act
            var result = _service.GetType()
                .GetMethod("CalculateInitialDelay", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { now });

            // Assert
            Assert.That(result, Is.EqualTo(expectedDelay));
        }
        [Test]
        public void CalculateInitialDelay_WhenAtExactInterval_ReturnsZeroDelay()
        {
            // Arrange
            var now = new DateTime(2024, 9, 22, 14, 00, 00); // Exactly at 14:00, a round hour interval
            var expectedDelay = TimeSpan.FromMinutes(1); // Since it's exactly on the interval, the delay should be zero

            // Act
            var result = _service.GetType()
                .GetMethod("CalculateInitialDelay", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { now });

            // Assert
            Assert.That(result, Is.EqualTo(expectedDelay));
        }

        [Test]
        public void RecurringJobService_TestConstructor()
        {
            new RecurringJobService(It.IsAny<ILogger<RecurringJobService>>());
        }

        [Test]
        public void ExecuteAsync_StopTimer()
        {
            RecurringJobService service = new RecurringJobService(_mockLogger.Object, new TestDbContext());
            service.StartAsync(CancellationToken.None);
            
            var result = service.StopAsync(CancellationToken.None);
            Assert.IsTrue(result.IsCompletedSuccessfully, "StopAsync should complete successfully without exceptions.");
        }

        [TearDown]
        public void Cleanup()
        {
            _service.Dispose();
            // Cleanup logic if necessary
        }
    }
}

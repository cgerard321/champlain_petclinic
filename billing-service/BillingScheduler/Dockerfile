# Use official .NET 9.0 runtime image
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS base
WORKDIR /app

FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src
COPY ./BillingScheduler.csproj ./
RUN dotnet restore "BillingScheduler.csproj"
COPY . .
WORKDIR "/src"
RUN dotnet build "BillingScheduler.csproj" -c Release -o /app/build
RUN dotnet publish "BillingScheduler.csproj" -c Release -o /app/publish

FROM base AS final
WORKDIR /app
COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "BillingScheduler.dll"]

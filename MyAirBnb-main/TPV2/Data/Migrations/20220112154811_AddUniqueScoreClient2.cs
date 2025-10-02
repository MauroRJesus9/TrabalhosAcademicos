using Microsoft.EntityFrameworkCore.Migrations;

namespace TPV2.Data.Migrations
{
    public partial class AddUniqueScoreClient2 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_ScoreClient_ReserveId",
                table: "ScoreClient");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreClient_ReserveId",
                table: "ScoreClient",
                column: "ReserveId",
                unique: true,
                filter: "[ReserveId] IS NOT NULL");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_ScoreClient_ReserveId",
                table: "ScoreClient");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreClient_ReserveId",
                table: "ScoreClient",
                column: "ReserveId");
        }
    }
}

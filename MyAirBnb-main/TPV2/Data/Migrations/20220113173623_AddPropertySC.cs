using Microsoft.EntityFrameworkCore.Migrations;

namespace TPV2.Data.Migrations
{
    public partial class AddPropertySC : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "PropertyId",
                table: "ScoreClient",
                type: "int",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_ScoreClient_PropertyId",
                table: "ScoreClient",
                column: "PropertyId");

            migrationBuilder.AddForeignKey(
                name: "FK_ScoreClient_Properties_PropertyId",
                table: "ScoreClient",
                column: "PropertyId",
                principalTable: "Properties",
                principalColumn: "PropertyId",
                onDelete: ReferentialAction.Restrict);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_ScoreClient_Properties_PropertyId",
                table: "ScoreClient");

            migrationBuilder.DropIndex(
                name: "IX_ScoreClient_PropertyId",
                table: "ScoreClient");

            migrationBuilder.DropColumn(
                name: "PropertyId",
                table: "ScoreClient");
        }
    }
}

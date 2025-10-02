using Microsoft.EntityFrameworkCore.Migrations;

namespace TPV2.Data.Migrations
{
    public partial class AddDB2 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Properties_PropertyCategories_PropertyCategoryId",
                table: "Properties");

            migrationBuilder.DropForeignKey(
                name: "FK_ScoreClient_Reserves_ReservesReserveId",
                table: "ScoreClient");

            migrationBuilder.DropForeignKey(
                name: "FK_ScoreGestor_Reserves_ReservesReserveId",
                table: "ScoreGestor");

            migrationBuilder.DropIndex(
                name: "IX_ScoreGestor_ReservesReserveId",
                table: "ScoreGestor");

            migrationBuilder.DropIndex(
                name: "IX_ScoreClient_ReservesReserveId",
                table: "ScoreClient");

            migrationBuilder.DropIndex(
                name: "IX_Properties_PropertyCategoryId",
                table: "Properties");

            migrationBuilder.DropColumn(
                name: "ReservesReserveId",
                table: "ScoreGestor");

            migrationBuilder.DropColumn(
                name: "ReservesReserveId",
                table: "ScoreClient");

            migrationBuilder.DropColumn(
                name: "PropertyCategoryId",
                table: "Properties");

            migrationBuilder.AlterColumn<int>(
                name: "CategoryId",
                table: "Properties",
                type: "int",
                nullable: false,
                defaultValue: 0,
                oldClrType: typeof(int),
                oldType: "int",
                oldNullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_ScoreGestor_ReserveId",
                table: "ScoreGestor",
                column: "ReserveId");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreClient_ReserveId",
                table: "ScoreClient",
                column: "ReserveId");

            migrationBuilder.CreateIndex(
                name: "IX_Properties_CategoryId",
                table: "Properties",
                column: "CategoryId");

            migrationBuilder.AddForeignKey(
                name: "FK_Properties_PropertyCategories_CategoryId",
                table: "Properties",
                column: "CategoryId",
                principalTable: "PropertyCategories",
                principalColumn: "CategoryId",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_ScoreClient_Reserves_ReserveId",
                table: "ScoreClient",
                column: "ReserveId",
                principalTable: "Reserves",
                principalColumn: "ReserveId",
                onDelete: ReferentialAction.Restrict);

            migrationBuilder.AddForeignKey(
                name: "FK_ScoreGestor_Reserves_ReserveId",
                table: "ScoreGestor",
                column: "ReserveId",
                principalTable: "Reserves",
                principalColumn: "ReserveId",
                onDelete: ReferentialAction.Restrict);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Properties_PropertyCategories_CategoryId",
                table: "Properties");

            migrationBuilder.DropForeignKey(
                name: "FK_ScoreClient_Reserves_ReserveId",
                table: "ScoreClient");

            migrationBuilder.DropForeignKey(
                name: "FK_ScoreGestor_Reserves_ReserveId",
                table: "ScoreGestor");

            migrationBuilder.DropIndex(
                name: "IX_ScoreGestor_ReserveId",
                table: "ScoreGestor");

            migrationBuilder.DropIndex(
                name: "IX_ScoreClient_ReserveId",
                table: "ScoreClient");

            migrationBuilder.DropIndex(
                name: "IX_Properties_CategoryId",
                table: "Properties");

            migrationBuilder.AddColumn<int>(
                name: "ReservesReserveId",
                table: "ScoreGestor",
                type: "int",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "ReservesReserveId",
                table: "ScoreClient",
                type: "int",
                nullable: true);

            migrationBuilder.AlterColumn<int>(
                name: "CategoryId",
                table: "Properties",
                type: "int",
                nullable: true,
                oldClrType: typeof(int),
                oldType: "int");

            migrationBuilder.AddColumn<int>(
                name: "PropertyCategoryId",
                table: "Properties",
                type: "int",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.CreateIndex(
                name: "IX_ScoreGestor_ReservesReserveId",
                table: "ScoreGestor",
                column: "ReservesReserveId");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreClient_ReservesReserveId",
                table: "ScoreClient",
                column: "ReservesReserveId");

            migrationBuilder.CreateIndex(
                name: "IX_Properties_PropertyCategoryId",
                table: "Properties",
                column: "PropertyCategoryId");

            migrationBuilder.AddForeignKey(
                name: "FK_Properties_PropertyCategories_PropertyCategoryId",
                table: "Properties",
                column: "PropertyCategoryId",
                principalTable: "PropertyCategories",
                principalColumn: "CategoryId",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_ScoreClient_Reserves_ReservesReserveId",
                table: "ScoreClient",
                column: "ReservesReserveId",
                principalTable: "Reserves",
                principalColumn: "ReserveId",
                onDelete: ReferentialAction.Restrict);

            migrationBuilder.AddForeignKey(
                name: "FK_ScoreGestor_Reserves_ReservesReserveId",
                table: "ScoreGestor",
                column: "ReservesReserveId",
                principalTable: "Reserves",
                principalColumn: "ReserveId",
                onDelete: ReferentialAction.Restrict);
        }
    }
}

using System;
using Microsoft.EntityFrameworkCore.Migrations;

namespace TPV2.Data.Migrations
{
    public partial class AddDB1 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<bool>(
                name: "isRemoved",
                table: "PropertyCategories",
                type: "bit",
                nullable: false,
                defaultValue: false);

            migrationBuilder.CreateTable(
                name: "Properties",
                columns: table => new
                {
                    PropertyId = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Name = table.Column<string>(type: "nvarchar(50)", maxLength: 50, nullable: false),
                    Description = table.Column<string>(type: "nvarchar(250)", maxLength: 250, nullable: false),
                    Price = table.Column<double>(type: "float", nullable: false),
                    Address = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: false),
                    CategoryId = table.Column<int>(type: "int", nullable: true),
                    PropertyCategoryId = table.Column<int>(type: "int", nullable: false),
                    ProprietarioId = table.Column<string>(type: "nvarchar(450)", nullable: false),
                    isRemoved = table.Column<bool>(type: "bit", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Properties", x => x.PropertyId);
                    table.ForeignKey(
                        name: "FK_Properties_AspNetUsers_ProprietarioId",
                        column: x => x.ProprietarioId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_Properties_PropertyCategories_PropertyCategoryId",
                        column: x => x.PropertyCategoryId,
                        principalTable: "PropertyCategories",
                        principalColumn: "CategoryId",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "ReserveStatus",
                columns: table => new
                {
                    StatusId = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Description = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: false),
                    isRemoved = table.Column<bool>(type: "bit", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ReserveStatus", x => x.StatusId);
                });

            migrationBuilder.CreateTable(
                name: "Reserves",
                columns: table => new
                {
                    ReserveId = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    CheckIn = table.Column<DateTime>(type: "datetime2", nullable: false),
                    CheckOut = table.Column<DateTime>(type: "datetime2", nullable: false),
                    PropertyId = table.Column<int>(type: "int", nullable: true),
                    Score = table.Column<int>(type: "int", nullable: false),
                    Observations = table.Column<string>(type: "nvarchar(250)", maxLength: 250, nullable: false),
                    StatusId = table.Column<int>(type: "int", nullable: true),
                    ClientId = table.Column<string>(type: "nvarchar(450)", nullable: true),
                    isRemoved = table.Column<bool>(type: "bit", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Reserves", x => x.ReserveId);
                    table.ForeignKey(
                        name: "FK_Reserves_AspNetUsers_ClientId",
                        column: x => x.ClientId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_Reserves_Properties_PropertyId",
                        column: x => x.PropertyId,
                        principalTable: "Properties",
                        principalColumn: "PropertyId",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_Reserves_ReserveStatus_StatusId",
                        column: x => x.StatusId,
                        principalTable: "ReserveStatus",
                        principalColumn: "StatusId",
                        onDelete: ReferentialAction.Restrict);
                });

            migrationBuilder.CreateTable(
                name: "ScoreClient",
                columns: table => new
                {
                    ScoreClientId = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    ClientId = table.Column<string>(type: "nvarchar(450)", nullable: true),
                    ReserveId = table.Column<int>(type: "int", nullable: true),
                    ReservesReserveId = table.Column<int>(type: "int", nullable: true),
                    Comments = table.Column<string>(type: "nvarchar(250)", maxLength: 250, nullable: false),
                    ScorePropriedade = table.Column<int>(type: "int", nullable: false),
                    ScoreProprietario = table.Column<int>(type: "int", nullable: false),
                    ProprietarioId = table.Column<string>(type: "nvarchar(450)", nullable: true),
                    ScoreFuncionário = table.Column<int>(type: "int", nullable: false),
                    FuncionarioId = table.Column<string>(type: "nvarchar(450)", nullable: true),
                    isRemoved = table.Column<bool>(type: "bit", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ScoreClient", x => x.ScoreClientId);
                    table.ForeignKey(
                        name: "FK_ScoreClient_AspNetUsers_ClientId",
                        column: x => x.ClientId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_ScoreClient_AspNetUsers_FuncionarioId",
                        column: x => x.FuncionarioId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_ScoreClient_AspNetUsers_ProprietarioId",
                        column: x => x.ProprietarioId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_ScoreClient_Reserves_ReservesReserveId",
                        column: x => x.ReservesReserveId,
                        principalTable: "Reserves",
                        principalColumn: "ReserveId",
                        onDelete: ReferentialAction.Restrict);
                });

            migrationBuilder.CreateTable(
                name: "ScoreGestor",
                columns: table => new
                {
                    ScoreGestorId = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    GestorId = table.Column<string>(type: "nvarchar(450)", nullable: true),
                    ReserveId = table.Column<int>(type: "int", nullable: true),
                    ReservesReserveId = table.Column<int>(type: "int", nullable: true),
                    Comments = table.Column<string>(type: "nvarchar(250)", maxLength: 250, nullable: false),
                    ScoreClient = table.Column<int>(type: "int", nullable: false),
                    ClientId = table.Column<string>(type: "nvarchar(450)", nullable: true),
                    isRemoved = table.Column<bool>(type: "bit", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ScoreGestor", x => x.ScoreGestorId);
                    table.ForeignKey(
                        name: "FK_ScoreGestor_AspNetUsers_ClientId",
                        column: x => x.ClientId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_ScoreGestor_AspNetUsers_GestorId",
                        column: x => x.GestorId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_ScoreGestor_Reserves_ReservesReserveId",
                        column: x => x.ReservesReserveId,
                        principalTable: "Reserves",
                        principalColumn: "ReserveId",
                        onDelete: ReferentialAction.Restrict);
                });

            migrationBuilder.CreateIndex(
                name: "IX_Properties_PropertyCategoryId",
                table: "Properties",
                column: "PropertyCategoryId");

            migrationBuilder.CreateIndex(
                name: "IX_Properties_ProprietarioId",
                table: "Properties",
                column: "ProprietarioId");

            migrationBuilder.CreateIndex(
                name: "IX_Reserves_ClientId",
                table: "Reserves",
                column: "ClientId");

            migrationBuilder.CreateIndex(
                name: "IX_Reserves_PropertyId",
                table: "Reserves",
                column: "PropertyId");

            migrationBuilder.CreateIndex(
                name: "IX_Reserves_StatusId",
                table: "Reserves",
                column: "StatusId");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreClient_ClientId",
                table: "ScoreClient",
                column: "ClientId");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreClient_FuncionarioId",
                table: "ScoreClient",
                column: "FuncionarioId");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreClient_ProprietarioId",
                table: "ScoreClient",
                column: "ProprietarioId");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreClient_ReservesReserveId",
                table: "ScoreClient",
                column: "ReservesReserveId");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreGestor_ClientId",
                table: "ScoreGestor",
                column: "ClientId");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreGestor_GestorId",
                table: "ScoreGestor",
                column: "GestorId");

            migrationBuilder.CreateIndex(
                name: "IX_ScoreGestor_ReservesReserveId",
                table: "ScoreGestor",
                column: "ReservesReserveId");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "ScoreClient");

            migrationBuilder.DropTable(
                name: "ScoreGestor");

            migrationBuilder.DropTable(
                name: "Reserves");

            migrationBuilder.DropTable(
                name: "Properties");

            migrationBuilder.DropTable(
                name: "ReserveStatus");

            migrationBuilder.DropColumn(
                name: "isRemoved",
                table: "PropertyCategories");
        }
    }
}
